package br.com.galsystem.construction.finance.service.serviceContract;

import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractCreateDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractFilterDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractUpdateDTO;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.ServiceContractMapper;
import br.com.galsystem.construction.finance.models.Category;
import br.com.galsystem.construction.finance.models.ServiceContract;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.CategoryRepository;
import br.com.galsystem.construction.finance.repository.ServiceContractRepository;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ServiceContractImpl implements serviceContract {

    private final ServiceContractRepository repository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceContractMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceContractDTO> listAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceContractDTO> findById(Long id) {
        if (id == null) return Optional.empty();

        return repository.findById(id)
                .map(mapper::toDTO)
                .or(() -> {
                    throw new ResourceNotFoundException(
                            "Serviço com ID %d não encontrado".formatted(id)
                    );
                });
    }

    @Override
    public ServiceContractDTO create(ServiceContractCreateDTO dto) {
        final ServiceContract entity = mapper.toEntity(dto);

        if (dto.supplierId() != null) {
            final Supplier supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fornecedor com ID %d não encontrado".formatted(dto.supplierId())));
            entity.setSupplier(supplier);
        }

        if (dto.categoryId() != null) {
            final Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Categoria com ID %d não encontrada".formatted(dto.categoryId())));
            entity.setCategory(category);
        }

        final ServiceContract saved = repository.save(entity);
        return mapper.toDTO(saved);

    }

    @Override
    @Transactional
    public ServiceContractDTO update(Long id, ServiceContractUpdateDTO dto) {
        ServiceContract entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço com ID %d não encontrado".formatted(id)));

        mapper.updateEntity(entity, dto);

        if (dto.supplierId() != null) {
            final Supplier supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fornecedor com ID %d não encontrado".formatted(dto.supplierId())));
            entity.setSupplier(supplier);
        }

        if (dto.categoryId() != null) {
            final Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Categoria com ID %d não encontrada".formatted(dto.categoryId())));
            entity.setCategory(category);
        }

        final ServiceContract updated = repository.save(entity);
        return mapper.toDTO(updated);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(
                    "Serviço com o ID %d não encontrado".formatted(id));
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceContractDTO> findByFilters(ServiceContractFilterDTO filters, Pageable pageable) {
        Specification<ServiceContract> spec = Specification.unrestricted();

        // 1. Otimização de Fetch (JOIN FETCH) para carregar os objetos relacionados e evitar campos nulos
        spec = spec.and((root, query, cb) -> {
            // Importante: Não fazemos fetch em queries de count (paginação), apenas na busca de dados
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("supplier", JoinType.LEFT);
                root.fetch("category", JoinType.LEFT);
                root.fetch("payments", JoinType.LEFT);
            }
            return null;
        });

        // 2. Filtros Dinâmicos
        if (filters.getName().isPresent()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filters.getName().get().toLowerCase() + "%"));
        }

        if (filters.getDescription().isPresent()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("description")), "%" + filters.getDescription().get().toLowerCase() + "%"));
        }

        if (filters.getSupplierId().isPresent()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("supplier").get("id"), filters.getSupplierId().get()));
        }

        if (filters.getCategoryId().isPresent()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), filters.getCategoryId().get()));
        }

        return repository.findAll(spec, pageable)
                .map(mapper::toDTO);
    }


    private Specification<ServiceContract> nameContains(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private Specification<ServiceContract> descriptionContains(String desc) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), "%" + desc.toLowerCase() + "%");
    }
}
