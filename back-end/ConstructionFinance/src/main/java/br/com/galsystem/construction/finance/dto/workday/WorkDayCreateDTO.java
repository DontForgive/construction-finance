package br.com.galsystem.construction.finance.dto.workday;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkDayCreateDTO {

    private LocalDate date;

    private Long supplierId;

    private BigDecimal hoursWorked;

    private BigDecimal dailyValue;

    private String note;
}
