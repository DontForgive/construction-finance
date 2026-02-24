package br.com.galsystem.construction.finance.dto.workday;

import java.time.LocalDate;
import java.util.List;

public record WorkDayBulkPaymentDTO(
        List<Long> workdayIds,
        Long supplierId,
        String description,
        LocalDate paymentDate,
        Long payerId,
        Long categoryId,
        Long serviceContractId,
        String paymentMethod,
        Double amount
) {
}
