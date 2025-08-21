package br.com.galsystem.construction.finance.controller.expense;

import br.com.galsystem.construction.finance.dto.expense.ChartDataDTO;
import br.com.galsystem.construction.finance.service.expense.ExpenseReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports/expenses")
public class ExpenseReportController {

    private final ExpenseReportService service;

    public ExpenseReportController(ExpenseReportService service) {
        this.service = service;
    }

    @GetMapping("/by-category")
    public List<ChartDataDTO> getByCategory() {
        return service.getTotalByCategory();
    }

    @GetMapping("/by-month")
    public List<ChartDataDTO> getByMonth() {
        return service.getTotalByMonth();
    }

    @GetMapping("/by-supplier")
    public List<ChartDataDTO> getBySupplier() {
        return service.getTotalBySupplier();
    }

    @GetMapping("/by-payment-method")
    public List<ChartDataDTO> getByPaymentMethod() {
        return service.getTotalByPaymentMethod();
    }

    @GetMapping("/by-payer")
    public List<ChartDataDTO> getByPayer() {
        return service.getTotalByPayer();
    }
}

