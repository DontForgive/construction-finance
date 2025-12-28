package br.com.galsystem.construction.finance.dto.serviceContract;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ServiceContractCreateDTO(
        Long supplierId,
        Long categoryId,
        String name,
        String description,
        BigDecimal contractedValue,
        LocalDate startDate,
        LocalDate endDate
) {
}
