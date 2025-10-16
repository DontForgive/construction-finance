package br.com.galsystem.construction.finance.dto.workday;

import br.com.galsystem.construction.finance.enums.WorkDayStatus;
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
public class WorkDayUpdateDTO {

    private LocalDate date;

    private BigDecimal hoursWorked;

    private BigDecimal dailyValue;

    private String note;

    private WorkDayStatus status;

}
