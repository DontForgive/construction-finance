package br.com.galsystem.construction.finance.service.supplier;

import br.com.galsystem.construction.finance.dto.supplier.CampoOpcional;
import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierListDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.InvalidPhoneException;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.SupplierMapper;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = "supplierList", key = "{#name, #worker, #pageable.pageNumber, #pageable.pageSize}")
    public Page<SupplierListDTO> listar(String name, String keyPix, Boolean worker, Pageable pageable) {
        return supplierRepository.findByFilters(name, keyPix, worker, pageable).map(supplierMapper::toListDTO);
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

        final String telefone = normalizarTelefone(dto.phone());
        Supplier entity = supplierMapper.toEntity(dto);
        entity.setName(name);
        entity.setKeyPix(keyPix);
        entity.setPhone(telefone);
        Supplier saved = supplierRepository.save(entity);
        return supplierMapper.toDTO(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "supplierList", allEntries = true),
            @CacheEvict(value = "supplierByName", key = "#dto.name().toLowerCase()", condition = "#dto.name() != null")
    })
    public SupplierDTO update(Long id, SupplierUpdateDTO dto) {
        Supplier entity = supplierRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Fornecedor com o ID %d não encontrado".formatted(id)));
        String name = dto.name().trim();
        String keyPix = Optional.ofNullable(dto.keyPix())
                .orElse("")
                .trim();

        if (!entity.getName().equalsIgnoreCase(name) && supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um Fornecedor com o nome '%s'".formatted(name));
        }

        if (supplierRepository.existsByKeyPixIgnoreCaseAndIdNot(keyPix, id)) {
            throw new ConflictException("CHAVE PIX já Cadastrada");
        }

        // Valida o telefone antes de alterar a entidade. Campo ausente mantém o valor atual.
        final boolean alterarTelefone = dto.phone() != null && dto.phone().isPresente();
        final String telefone = alterarTelefone ? normalizarTelefone(dto.phone().valor()) : null;

        supplierMapper.updateEntity(entity, dto);
        entity.setName(name);
        entity.setKeyPix(keyPix);
        if (alterarTelefone) {
            entity.setPhone(telefone);
        }
        Supplier updated = supplierRepository.save(entity);
        return supplierMapper.toDTO(updated);
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

    /**
     * Remove a máscara do telefone e valida 10 ou 11 dígitos (DDD incluso).
     * Vazio ou nulo vira null. Letras ou quantidade de dígitos fora do intervalo são rejeitadas.
     */
    private static String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }
        if (telefone.matches(".*\\p{L}.*")) {
            throw new InvalidPhoneException();
        }
        final String digitos = telefone.replaceAll("\\D", "");
        if (!digitos.matches("\\d{10,11}")) {
            throw new InvalidPhoneException();
        }
        return digitos;
    }
}
