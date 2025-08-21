package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.expense.ChartDataDTO;
import br.com.galsystem.construction.finance.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExpenseReportService {

    private final ExpenseRepository repository;

    public ExpenseReportService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<ChartDataDTO> getTotalByCategory() {
        return repository.getTotalByCategory().stream()
                .map(obj -> new ChartDataDTO((String) obj[0], (BigDecimal) obj[1]))
                .toList();
    }

    public List<ChartDataDTO> getTotalByMonth() {
        return repository.getTotalByMonth().stream()
                .map(obj -> new ChartDataDTO("Mês " + obj[0], (BigDecimal) obj[1]))
                .toList();
    }

    public List<ChartDataDTO> getTotalBySupplier() {
        return repository.getTotalBySupplier().stream()
                .map(obj -> new ChartDataDTO((String) obj[0], (BigDecimal) obj[1]))
                .toList();
    }

    public List<ChartDataDTO> getTotalByPaymentMethod() {
        return repository.getTotalByPaymentMethod().stream()
                .map(obj -> new ChartDataDTO((String) obj[0], (BigDecimal) obj[1]))
                .toList();
    }

    public List<ChartDataDTO> getTotalByPayer() {
        return repository.getTotalByPayer().stream()
                .map(obj -> new ChartDataDTO((String) obj[0], (BigDecimal) obj[1]))
                .toList();
    }
}
