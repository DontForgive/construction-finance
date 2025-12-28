package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.models.Payer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PayerMapper {

    PayerDTO toDTO(Payer entity);

    Payer toEntity(PayerCreateDTO dto);

    void updateEntity(@MappingTarget Payer entity, PayerUpdateDTO dto);
}
