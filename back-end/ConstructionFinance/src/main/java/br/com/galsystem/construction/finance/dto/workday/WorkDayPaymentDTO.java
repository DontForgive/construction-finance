package br.com.galsystem.construction.finance.dto.workday;

import java.time.LocalDate;
import java.util.List;

public record WorkDayPaymentDTO(
        List<Long> workdayIds,
        String description,
        LocalDate paymentDate,
        Long supplierId,
        Long categoryId
) {
}
