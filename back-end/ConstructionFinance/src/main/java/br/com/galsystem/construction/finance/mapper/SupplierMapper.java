package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.models.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDTO toDTO(Supplier entity);

    SupplierDetailDTO toDetailDTO(Supplier entity);

    Supplier toEntity(SupplierCreateDTO dto);
}
