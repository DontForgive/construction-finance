package br.com.galsystem.construction.finance.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDTO(
        Long id,
        LocalDate date,
        String description,
        Long supplierId,
        String supplierName,
        Long payerId,
        String payerName,
        Long categoryId,
        String categoryName,
        String paymentMethod,
        BigDecimal amount,
        String attachmentUrl
) {}
