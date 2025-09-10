package br.com.galsystem.construction.finance.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUploadByFileDTO(
        LocalDate date,
        String description,
        String supplierName,
        String payerName,
        String categoryName,
        String paymentMethod,
        BigDecimal amount,
        String attachmentUrl
) {}
