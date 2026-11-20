package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierListDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.models.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDTO toDTO(Supplier entity);

    SupplierListDTO toListDTO(Supplier entity);

    SupplierDetailDTO toDetailDTO(Supplier entity);

    Supplier toEntity(SupplierCreateDTO dto);

    // phone é tratado no serviço (presença, normalização e validação)
    @Mapping(target = "phone", ignore = true)
    void updateEntity(@MappingTarget Supplier entity, SupplierUpdateDTO dto);
}
