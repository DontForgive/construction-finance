package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.expense.*;
import br.com.galsystem.construction.finance.models.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    @Mapping(source = "payer.id", target = "payerId")
    @Mapping(source = "payer.name", target = "payerName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ExpenseDTO toDTO(Expense entity);

    // Associações (supplier/payer) são setadas no service
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "payer", ignore = true)
    Expense toEntity(ExpenseCreateDTO dto);

    // Atualiza campos simples; associações tratadas no service
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "payer", ignore = true)
    void updateEntity(@MappingTarget Expense entity, ExpenseUpdateDTO dto);
}
