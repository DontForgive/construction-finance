package br.com.galsystem.construction.finance.dto.charts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseKpiDTO {
    private BigDecimal totalExpenses;
    private Long totalPayers;
    private Long totalSuppliers;
    private Long totalCategories;
}
