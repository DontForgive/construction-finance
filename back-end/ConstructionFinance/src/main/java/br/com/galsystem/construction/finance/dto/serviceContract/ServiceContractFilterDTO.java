package br.com.galsystem.construction.finance.dto.serviceContract;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class ServiceContractFilterDTO {
    private Optional<String> name;
    private Optional<String> description;
    private Optional<Long> supplierId;
    private Optional<Long> categoryId;

}
