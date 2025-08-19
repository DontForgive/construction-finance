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
    public PayerDTO create(PayerCreateDTO dto) {
        String name = dto.name().trim();

        if(repository.existsByNameIgnoreCase(name)){
            throw new ConflictException("Já existe um pagador com nome '%s'".formatted(name));
        }
        Payer entity = mapper.toEntity(dto);
        entity.setName(name);
        Payer saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public PayerDTO update(Long id, PayerUpdateDTO dto) {
        Payer entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagador com ID %d não encontrado".formatted(id)));

        String name = dto.name().trim();
        if (!entity.getName().equalsIgnoreCase(name)
                && repository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe uma categoria com nome '%s'".formatted(name));
        }
        mapper.updateEntity(entity, dto);
        entity.setName(name);
        Payer updated = repository.save(entity);
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        if(!repository.existsById(id)){
            throw new NotFoundException("Pagador com o ID %d não encontrado".formatted(id));
        }

        repository.deleteById(id);

    }
}
