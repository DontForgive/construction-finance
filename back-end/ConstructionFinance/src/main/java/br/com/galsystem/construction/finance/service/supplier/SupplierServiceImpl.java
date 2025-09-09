package br.com.galsystem.construction.finance.service.supplier;
import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.SupplierMapper;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> listar(String name, Pageable pageable) {
        return supplierRepository.findByFilters(name, pageable).map(supplierMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO findById(Long id) {
        Supplier entity = supplierRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Fornecedor com ID %d não encontrado".formatted(id)));
        return supplierMapper.toDTO(entity);
    }

    @Override
    public SupplierDTO findOrCreateByName(String name) {
        return supplierRepository.findByNameIgnoreCase(name)
                .map(supplierMapper::toDTO)
                .orElseGet(() -> createAndCache(name));
    }

    @Override
    @Cacheable(value = "supplierByName", key = "#name")
    public SupplierDTO createAndCache(String name) {
        Supplier entity = new Supplier();
        entity.setName(name);
        entity = supplierRepository.save(entity);
        return supplierMapper.toDTO(entity);
    }

    @Override
    @Transactional
    @CachePut(value = "supplierByName", key = "#name.toLowerCase()")
    public SupplierDTO create(SupplierCreateDTO dto) {
        String name = dto.name().trim();
        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("já existe um Fornecedor com o nome '%s'".formatted(name));
        }
        Supplier entity = supplierMapper.toEntity(dto);
        entity.setName(name);
        Supplier saved = supplierRepository.save(entity);
        return supplierMapper.toDTO(saved);

    }

    @Override
    @Transactional
    public SupplierDTO update(Long id, SupplierUpdateDTO dto) {
        Supplier entity = supplierRepository.findById(id).orElseThrow(
                ()-> new NotFoundException("Fornecedor com o ID %d não encontrado".formatted(id)));
        String name = dto.name().trim();

        if (!entity.getName().equalsIgnoreCase(name) && supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um Fornecedor com o nome %s".formatted(name));
        }
        supplierMapper.updateEntity(entity, dto);
        entity.setName(name);
        Supplier updated = supplierRepository.save(entity);
        return supplierMapper.toDTO(updated);

    }

    @Override
    @Transactional
    public void delete(Long id) {
        if(!supplierRepository.existsById(id)) {
            throw new NotFoundException("Fornecedor com o ID %d não encontrado".formatted(id));
        }
        supplierRepository.deleteById(id);
    }

}
