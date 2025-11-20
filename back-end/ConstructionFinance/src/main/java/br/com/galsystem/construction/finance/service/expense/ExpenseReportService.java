package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.charts.ExpenseKpiDTO;
import br.com.galsystem.construction.finance.dto.expense.ChartDataDTO;
import br.com.galsystem.construction.finance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ExpenseReportService {

    private final ExpenseRepository repository;

    // --- Por Categoria
    public List<ChartDataDTO> getTotalByCategory(LocalDate start,
                                                 LocalDate end,
                                                 Long userId,
                                                 Long categoryId,
                                                 Long supplierId,
                                                 Long payerId) {

        LocalDate safeStart = start != null ? start : LocalDate.of(1900, 1, 1);
        LocalDate safeEnd = end != null ? end : LocalDate.of(2100, 12, 31);

        return repository.getTotalByCategory(safeStart, safeEnd, userId, categoryId, supplierId, payerId);
    }

    // --- Por Mês
    public List<ChartDataDTO> getTotalByMonth(LocalDate start,
                                              LocalDate end,
                                              Long userId,
                                              Long categoryId,
                                              Long supplierId,
                                              Long payerId) {

        LocalDate safeStart = start != null ? start : LocalDate.of(1900, 1, 1);
        LocalDate safeEnd = end != null ? end : LocalDate.of(2100, 12, 31);

        return repository.getTotalByMonth(safeStart, safeEnd, userId, categoryId, supplierId, payerId);
    }

    // --- Por Fornecedor
    public List<ChartDataDTO> getTotalBySupplier(LocalDate start,
                                                 LocalDate end,
                                                 Long userId,
                                                 Long categoryId,
                                                 Long payerId) {

        LocalDate safeStart = start != null ? start : LocalDate.of(1900, 1, 1);
        LocalDate safeEnd = end != null ? end : LocalDate.of(2100, 12, 31);

        return repository.getTotalBySupplier(safeStart, safeEnd, userId, categoryId, payerId);
    }

    // --- Por Método de Pagamento
    public List<ChartDataDTO> getTotalByPaymentMethod(LocalDate start,
                                                      LocalDate end,
                                                      Long userId,
                                                      Long categoryId,
                                                      Long supplierId,
                                                      Long payerId) {

        LocalDate safeStart = start != null ? start : LocalDate.of(1900, 1, 1);
        LocalDate safeEnd = end != null ? end : LocalDate.of(2100, 12, 31);

        return repository.getTotalByPaymentMethod(safeStart, safeEnd, userId, categoryId, supplierId, payerId);
    }

    // --- Por Pagador
    public List<ChartDataDTO> getTotalByPayer(LocalDate start,
                                              LocalDate end,
                                              Long userId,
                                              Long categoryId,
                                              Long supplierId) {

        LocalDate safeStart = start != null ? start : LocalDate.of(1900, 1, 1);
        LocalDate safeEnd = end != null ? end : LocalDate.of(2100, 12, 31);

        return repository.getTotalByPayer(safeStart, safeEnd, userId, categoryId, supplierId);
    }

    public ExpenseKpiDTO getKpis(
            LocalDate start,
            LocalDate end,
            Long categoryId,
            Long supplierId,
            Long payerId
    ) {
        // 🔹 Total de despesas = FILTRADO
        BigDecimal totalExpenses = repository.getTotalExpenses(
                start, end, categoryId, supplierId, payerId
        );

        // 🔹 As contagens serão SEMPRE gerais (null null null null null)
        Long totalPayers = repository.getTotalPayers(null, null, null, null, null);
        Long totalSuppliers = repository.getTotalSuppliers(null, null, null, null, null);
        Long totalCategories = repository.getTotalCategories(null, null, null, null, null);

        return new ExpenseKpiDTO(
                totalExpenses != null ? totalExpenses : BigDecimal.ZERO,
                totalPayers != null ? totalPayers : 0,
                totalSuppliers != null ? totalSuppliers : 0,
                totalCategories != null ? totalCategories : 0
        );
    }


}
