package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractCreateDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractUpdateDTO;
import br.com.galsystem.construction.finance.models.ServiceContract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ExpenseMapper.class})
public interface ServiceContractMapper {

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "totalPaid", expression = "java(entity.getTotalPaid())")
    @Mapping(target = "balance", expression = "java(entity.getBalance())")
    ServiceContractDTO toDTO(ServiceContract entity);

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "payments", expression = "java(new java.util.ArrayList<>())")
    ServiceContract toEntity(ServiceContractCreateDTO dto);

    @Mapping(target = "id", ignore = true) // ADICIONE ESTA LINHA
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateEntity(@MappingTarget ServiceContract entity, ServiceContractUpdateDTO dto);
}


