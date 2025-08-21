package br.com.galsystem.construction.finance.dto.expense;
import lombok.Data;
import java.math.BigDecimal;


@Data
public class ChartDataDTO {
    private String label;
    private BigDecimal value;

    public ChartDataDTO(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }
}
