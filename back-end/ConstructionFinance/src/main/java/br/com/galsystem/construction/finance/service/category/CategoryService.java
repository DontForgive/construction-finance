package br.com.galsystem.construction.finance.service.category;

import br.com.galsystem.construction.finance.dto.category.CategoryCreateDTO;
import br.com.galsystem.construction.finance.dto.category.CategoryDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.models.Category;
import br.com.galsystem.construction.finance.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public CategoryDTO create(CategoryCreateDTO dto) {
        String name = dto.getName().trim();
        String description = dto.getDescription().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe uma categoria com o nome informado.");
        }

        Category entity = new Category();
        entity.setName(name);
        entity.setDescription(description);

        try {
            return toDTO(categoryRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ocorre um erro: " + e.getMessage());
        }
    }

    private CategoryDTO toDTO(Category p) {
        CategoryDTO out = new CategoryDTO();
        out.setId(p.getId());
        out.setName(p.getName());
        out.setDescription(p.getDescription());
        return out;
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
