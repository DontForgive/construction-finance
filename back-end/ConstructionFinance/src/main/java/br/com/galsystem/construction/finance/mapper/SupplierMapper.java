package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.models.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDTO toDTO(Supplier entity);

    @Mapping(target = "cep", source = "cep", qualifiedByName = "cepSomenteDigitos")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "ufMaiuscula")
    Supplier toEntity(SupplierCreateDTO dto);

    void updateEntity(@MappingTarget Supplier entity, SupplierUpdateDTO dto);

    @Named("cepSomenteDigitos")
    default String cepSomenteDigitos(String cep) {
        return cep == null ? null : cep.replaceAll("\\D", "");
    }

    @Named("ufMaiuscula")
    default String ufMaiuscula(String estado) {
        return estado == null ? null : estado.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
