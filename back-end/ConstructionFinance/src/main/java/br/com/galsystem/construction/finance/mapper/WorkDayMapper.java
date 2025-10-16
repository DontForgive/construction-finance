package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.workday.WorkDayCreateDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayUpdateDTO;
import br.com.galsystem.construction.finance.models.WorkDay;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkDayMapper {

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    WorkDayDTO toDTO(WorkDay entity);

    WorkDay toEntity(WorkDayCreateDTO dto);

    void updateEntity(@MappingTarget WorkDay entity, WorkDayUpdateDTO dto);
}
