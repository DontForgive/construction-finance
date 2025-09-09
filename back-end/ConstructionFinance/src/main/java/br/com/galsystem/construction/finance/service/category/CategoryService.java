package br.com.galsystem.construction.finance.service.category;

import br.com.galsystem.construction.finance.dto.category.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryDTO> list(String name, String description, Pageable pageable);
    CategoryDTO findById(Long id);
    CategoryDTO findOrCreateByName(String name);
    CategoryDTO create(CategoryCreateDTO dto);
    CategoryDTO update(Long id, CategoryUpdateDTO dto);
    void delete(Long id);
}
