package br.com.galsystem.construction.finance.service.category;

import br.com.galsystem.construction.finance.dto.category.*;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.mapper.CategoryMapper;
import br.com.galsystem.construction.finance.models.Category;
import br.com.galsystem.construction.finance.repository.CategoryRepository;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categorylist", key = "{#name, #description, #pageable.pageNumber, #pageable.pageSize}")
    public Page<CategoryDTO> list(String name, String description, Pageable pageable) {
        return repository.findByFilters(name, description, pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryfindById", key = "#id")
    public CategoryDTO findById(Long id) {
        Category entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria com ID %d não encontrada".formatted(id)));
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public CategoryDTO findOrCreateByName(String name) {
        final String normalized = name.trim();
        return repository.findByNameIgnoreCase(normalized)
                .map(mapper::toDTO)
                .orElseGet(() -> createAndCacheInternal(normalized));
    }

    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "categoryByName", key = "#name.toLowerCase()")
            },
            evict = {
                    @CacheEvict(value = {"categorylist"}, allEntries = true)
            }
    )
    protected CategoryDTO createAndCacheInternal(String name) {
        try {
            Category entity = mapper.toEntity(new CategoryCreateDTO(name, "Criado automaticamente via importação"));
            Category saved = repository.save(entity);
            return mapper.toDTO(saved);
        } catch (DataIntegrityViolationException ex) {
            Category existing = repository.findByNameIgnoreCase(name)
                    .orElseThrow(() -> ex);
            return mapper.toDTO(existing);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categorylist", "categoryByName", "categoryfindById"}, allEntries = true)
    public CategoryDTO create(CategoryCreateDTO dto) {
        String normalized = dto.name().trim();
        if (repository.existsByNameIgnoreCase(normalized)) {
            throw new ConflictException("Já existe uma categoria com nome '%s'".formatted(normalized));
        }
        Category entity = mapper.toEntity(dto);
        entity.setName(normalized);
        Category saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categorylist", "categoryByName", "categoryfindById"}, allEntries = true)
    public CategoryDTO update(Long id, CategoryUpdateDTO dto) {
        Category entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria com ID %d não encontrada".formatted(id)));

        String normalized = dto.name().trim();
        if (!entity.getName().equalsIgnoreCase(normalized)
                && repository.existsByNameIgnoreCase(normalized)) {
            throw new ConflictException("Já existe uma categoria com nome '%s'".formatted(normalized));
        }

        mapper.updateEntity(entity, dto);
        entity.setName(normalized);
        Category saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categorylist", "categoryByName", "categoryfindById"}, allEntries = true)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Categoria com ID %d não encontrada".formatted(id));
        }
        repository.deleteById(id);
    }
}
