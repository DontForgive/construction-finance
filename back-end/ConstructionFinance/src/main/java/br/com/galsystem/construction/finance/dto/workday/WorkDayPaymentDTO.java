package br.com.galsystem.construction.finance.dto.workday;

import java.time.LocalDate;
import java.util.List;

public record WorkDayPaymentDTO(
        List<Long> workDayIds,
        String description,
        LocalDate paymentDate,
        Long payToSupplierId,
        Long categoryId // opcional

) {
}
