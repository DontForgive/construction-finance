package br.com.galsystem.construction.finance.service.payer;

import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.PayerMapper;
import br.com.galsystem.construction.finance.models.Payer;
import br.com.galsystem.construction.finance.repository.PayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayerServiceImpl implements PayerService {

    private final PayerRepository repository;
    private final PayerMapper mapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "payerList", key = "{#name, #pageable.pageNumber, #pageable.pageSize}")
    public Page<PayerDTO> listar(String name, Pageable pageable) {
        return repository.findByFilters(name, pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PayerDTO findById(Long id) {
        Payer entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagador com ID %d não encontrado".formatted(id)));
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "payerList", allEntries = true)
    })
    public PayerDTO create(PayerCreateDTO dto) {
        String name = dto.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um pagador com nome '%s'".formatted(name));
        }
        Payer entity = mapper.toEntity(dto);
        entity.setName(name);
        Payer saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "payerList", allEntries = true),
            @CacheEvict(value = "payerByName", key = "#dto.name().toLowerCase()", condition = "#dto.name() != null")
    })
    public PayerDTO update(Long id, PayerUpdateDTO dto) {
        Payer entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagador com ID %d não encontrado".formatted(id)));

        String name = dto.name().trim();
        if (!entity.getName().equalsIgnoreCase(name) && repository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um pagador com nome '%s'".formatted(name));
        }
        mapper.updateEntity(entity, dto);
        entity.setName(name);
        Payer updated = repository.save(entity);
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "payerList", allEntries = true)
    })
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Pagador com o ID %d não encontrado".formatted(id));
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public PayerDTO findOrCreateByName(String name) {
        final String normalized = name.trim();
        // Primeiro tenta buscar do repositório (rápido + evita poluir cache com misses)
        return repository.findByNameIgnoreCase(normalized)
                .map(mapper::toDTO)
                .orElseGet(() -> createAndCache(normalized));
    }

    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "payerByName", key = "#name.toLowerCase()")
            },
            evict = {
                    @CacheEvict(value = "payerList", allEntries = true)
            }
    )
    public PayerDTO createAndCache(String name) {
        final String normalized = name.trim();
        try {
            Payer entity = new Payer();
            entity.setName(normalized);
            entity = repository.save(entity);
            return mapper.toDTO(entity);
        } catch (DataIntegrityViolationException ex) {
            // Concorrência: outro request criou simultaneamente. Recarrega.
            Payer existing = repository.findByNameIgnoreCase(normalized)
                    .orElseThrow(() -> ex);
            return mapper.toDTO(existing);
        }
    }
}
