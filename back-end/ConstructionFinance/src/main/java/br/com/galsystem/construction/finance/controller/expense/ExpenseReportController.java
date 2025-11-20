package br.com.galsystem.construction.finance.controller.expense;

import br.com.galsystem.construction.finance.dto.charts.ExpenseKpiDTO;
import br.com.galsystem.construction.finance.dto.expense.ChartDataDTO;
import br.com.galsystem.construction.finance.service.expense.ExpenseReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/reports/expenses")
@RequiredArgsConstructor
public class ExpenseReportController {

    private final ExpenseReportService service;

    // --- Por Categoria
    @GetMapping("/by-category")
    public List<ChartDataDTO> getByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long payerId
    ) {
        return service.getTotalByCategory(start, end, userId, categoryId, supplierId, payerId);
    }

    // --- Por Mês
    @GetMapping("/by-month")
    public List<ChartDataDTO> getByMonth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long payerId
    ) {
        return service.getTotalByMonth(start, end, userId, categoryId, supplierId, payerId);
    }

    // --- Por Fornecedor
    @GetMapping("/by-supplier")
    public List<ChartDataDTO> getBySupplier(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long payerId
    ) {
        return service.getTotalBySupplier(start, end, userId, categoryId, payerId);
    }

    // --- Por Método de Pagamento
    @GetMapping("/by-payment-method")
    public List<ChartDataDTO> getByPaymentMethod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long payerId
    ) {
        return service.getTotalByPaymentMethod(start, end, userId, categoryId, supplierId, payerId);
    }

    // --- Por Pagador
    @GetMapping("/by-payer")
    public List<ChartDataDTO> getByPayer(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId
    ) {
        return service.getTotalByPayer(start, end, userId, categoryId, supplierId);
    }

    @GetMapping("/kpis")
    public ResponseEntity<ExpenseKpiDTO> getKpis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long payerId
    ) {
        return ResponseEntity.ok(service.getKpis(start, end, categoryId, supplierId, payerId));
    }
}
