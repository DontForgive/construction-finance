package br.com.galsystem.construction.finance.service.supplier;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.SupplierMapper;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = "supplierList", key = "{#name, #worker, #pageable.pageNumber, #pageable.pageSize}")
    public Page<SupplierDTO> listar(String name, String keyPix, Boolean worker, Pageable pageable) {
        return supplierRepository.findByFilters(name, keyPix, worker, pageable).map(supplierMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDetailDTO findById(Long id) {
        Supplier entity = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor com ID %d não encontrado".formatted(id)));
        return supplierMapper.toDetailDTO(entity);
    }

    @Override
    @Transactional
    public SupplierDTO findOrCreateByName(String name) {
        final String normalized = name.trim();
        return supplierRepository.findByNameIgnoreCase(normalized)
                .map(supplierMapper::toDTO)
                .orElseGet(() -> createAndCache(normalized));
    }

    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "supplierByName", key = "#name.toLowerCase()")
            },
            evict = {
                    @CacheEvict(value = "supplierList", allEntries = true)
            }
    )
    public SupplierDTO createAndCache(String name) {
        final String normalized = name.trim();
        try {
            Supplier entity = new Supplier();
            entity.setName(normalized);
            entity = supplierRepository.save(entity);
            return supplierMapper.toDTO(entity);
        } catch (DataIntegrityViolationException ex) {
            Supplier existing = supplierRepository.findByNameIgnoreCase(normalized)
                    .orElseThrow(() -> ex);
            return supplierMapper.toDTO(existing);
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "supplierList", allEntries = true),
            @CacheEvict(value = "supplierByName", key = "#dto.name().toLowerCase()", condition = "#dto.name() != null")
    })
    public SupplierDTO create(SupplierCreateDTO dto) {
        String name = dto.name().trim();
        String keyPix = Optional.ofNullable(dto.keyPix())
                .orElse("")
                .trim();

        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um Fornecedor com o nome '%s'".formatted(name));
        }

        if (supplierRepository.existsByKeyPixIgnoreCase(keyPix)) {
            throw new ConflictException("CHAVE PIX já Cadastrada");
        }

        Supplier entity = supplierMapper.toEntity(dto);
        entity.setName(name);
        entity.setKeyPix(keyPix);
        Supplier saved = supplierRepository.save(entity);
        return supplierMapper.toDTO(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "supplierList", allEntries = true),
            @CacheEvict(value = "supplierByName", key = "#dto.getName().toLowerCase()", condition = "#dto.getName() != null")
    })
    public SupplierDTO update(Long id, SupplierUpdateDTO dto) {
        Supplier entity = supplierRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Fornecedor com o ID %d não encontrado".formatted(id)));
        String name = dto.getName().trim();
        String keyPix = Optional.ofNullable(dto.getKeyPix())
                .orElse("")
                .trim();

        if (!entity.getName().equalsIgnoreCase(name) && supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um Fornecedor com o nome '%s'".formatted(name));
        }

        if (supplierRepository.existsByKeyPixIgnoreCaseAndIdNot(keyPix, id)) {
            throw new ConflictException("CHAVE PIX já Cadastrada");
        }

        final String oldName = entity.getName();

        entity.setName(name);
        entity.setWorker(dto.getWorker());
        entity.setKeyPix(keyPix);
        applyAddress(entity, dto);
        Supplier updated = supplierRepository.save(entity);
        evictSupplierByName(oldName);
        return supplierMapper.toDTO(updated);
    }

    /**
     * Invalida a chave do nome anterior em supplierByName. A chave do nome novo e supplierList
     * continuam sendo invalidadas pelas anotações de {@link #update}.
     */
    private void evictSupplierByName(final String name) {
        if (name == null) {
            return;
        }
        Cache cache = cacheManager.getCache("supplierByName");
        if (cache != null) {
            cache.evict(name.toLowerCase());
        }
    }

    /**
     * Aplica o endereço campo a campo: ausente mantém o valor atual; null ou vazio limpa; valor atualiza.
     * Os valores já chegam normalizados pelos setters do DTO.
     */
    private void applyAddress(final Supplier entity, final SupplierUpdateDTO dto) {
        if (dto.isPresent(SupplierUpdateDTO.ZIP_CODE)) {
            entity.setZipCode(dto.getZipCode());
        }
        if (dto.isPresent(SupplierUpdateDTO.STREET)) {
            entity.setStreet(dto.getStreet());
        }
        if (dto.isPresent(SupplierUpdateDTO.NUMBER)) {
            entity.setNumber(dto.getNumber());
        }
        if (dto.isPresent(SupplierUpdateDTO.COMPLEMENT)) {
            entity.setComplement(dto.getComplement());
        }
        if (dto.isPresent(SupplierUpdateDTO.NEIGHBORHOOD)) {
            entity.setNeighborhood(dto.getNeighborhood());
        }
        if (dto.isPresent(SupplierUpdateDTO.CITY)) {
            entity.setCity(dto.getCity());
        }
        if (dto.isPresent(SupplierUpdateDTO.STATE)) {
            entity.setState(dto.getState());
        }
        if (dto.isPresent(SupplierUpdateDTO.COUNTRY)) {
            entity.setCountry(dto.getCountry());
        }
        if (dto.isPresent(SupplierUpdateDTO.REFERENCE)) {
            entity.setReference(dto.getReference());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"supplierList"}, allEntries = true)
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new NotFoundException("Fornecedor com o ID %d não encontrado".formatted(id));
        }
        supplierRepository.deleteById(id);
    }
}
