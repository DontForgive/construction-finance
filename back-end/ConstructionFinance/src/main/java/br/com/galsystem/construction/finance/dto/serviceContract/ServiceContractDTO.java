package br.com.galsystem.construction.finance.dto.serviceContract;

import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServiceContractDTO(
        Long id,
        Long supplierId,
        String supplierName,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal contractedValue,
        BigDecimal totalPaid,
        BigDecimal balance,
        LocalDate startDate,
        LocalDate endDate,
        List<ExpenseDTO> payments
) {
}

