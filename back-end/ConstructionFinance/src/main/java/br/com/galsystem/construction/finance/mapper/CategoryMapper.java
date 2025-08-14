package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.category.*;
import br.com.galsystem.construction.finance.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category entity);
    Category toEntity(CategoryCreateDTO dto);
    void updateEntity(@MappingTarget Category entity, CategoryUpdateDTO dto);
}
