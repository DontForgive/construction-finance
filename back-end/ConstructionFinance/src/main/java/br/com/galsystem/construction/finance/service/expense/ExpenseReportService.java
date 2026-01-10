package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.charts.ExpenseKpiDTO;
import br.com.galsystem.construction.finance.dto.expense.ChartDataDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ExpenseReportService {

    private final ExpenseRepository repository;
    private final ExpenseService expenseService;

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
            Long payerId,
            Long serviceContractId
    ) {
        // 🔹 Total de despesas = FILTRADO
        BigDecimal totalExpenses = repository.getTotalExpenses(
                start, end, categoryId, supplierId, payerId, serviceContractId
        );

        // 🔹 As contagens serão SEMPRE gerais (null null null null null)
        Long totalPayers = repository.getTotalPayers(null, null, null, null, null, null);
        Long totalSuppliers = repository.getTotalSuppliers(null, null, null, null, null, null);
        Long totalCategories = repository.getTotalCategories(null, null, null, null, null, null);

        return new ExpenseKpiDTO(
                totalExpenses != null ? totalExpenses : BigDecimal.ZERO,
                totalPayers != null ? totalPayers : 0,
                totalSuppliers != null ? totalSuppliers : 0,
                totalCategories != null ? totalCategories : 0
        );
    }

    // TODO - function generateXLSX()

    public byte[] generateExpensesXLSX(LocalDate startDate,
                                       LocalDate endDate,
                                       Long supplierId,
                                       Long payerId,
                                       Long categoryId,
                                       Long serviceContractId,
                                       String paymentMethod
    ) {
        List<ExpenseDTO> expenses = expenseService.listAll(startDate, endDate, supplierId, payerId, categoryId, serviceContractId, paymentMethod);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Pagamentos");
            CreationHelper helper = wb.getCreationHelper();

            // Estilos
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy"));

            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0.00"));

            String[] columns = {"Data", "Descrição", "Valor", "Fornecedor", "Pagador", "Categoria", "Método", "Contrato"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            BigDecimal totalValue = BigDecimal.ZERO;

            for (ExpenseDTO expense : expenses) {
                Row row = sheet.createRow(rowNum++);

                Cell dateCell = row.createCell(0);
                if (expense.date() != null) {
                    dateCell.setCellValue(expense.date());
                    dateCell.setCellStyle(dateStyle);
                }

                row.createCell(1).setCellValue(expense.description());

                Cell amountCell = row.createCell(2);
                if (expense.amount() != null) {
                    amountCell.setCellValue(expense.amount().doubleValue());
                    amountCell.setCellStyle(currencyStyle);
                }

                row.createCell(3).setCellValue(expense.supplierName());
                row.createCell(4).setCellValue(expense.payerName());
                row.createCell(5).setCellValue(expense.categoryName());
                row.createCell(6).setCellValue(expense.paymentMethod());
                row.createCell(7).setCellValue(expense.serviceContractName());

                totalValue = totalValue.add(expense.amount());
            }

            Row summaryRow = sheet.createRow(rowNum + 1);
            summaryRow.createCell(0).setCellValue("Total de Pagamentos:" + expenses.size());

            summaryRow.createCell(1).setCellValue("Valor Pago: ");
            summaryRow.createCell(2).setCellValue("R$ " + totalValue.doubleValue());

            summaryRow.getCell(0).setCellStyle(currencyStyle);
            summaryRow.getCell(1).setCellStyle(currencyStyle);
            summaryRow.getCell(2).setCellStyle(currencyStyle);

            summaryRow.getCell(0).setCellStyle(headerStyle);
            summaryRow.getCell(1).setCellStyle(headerStyle);
            summaryRow.getCell(2).setCellStyle(headerStyle);


            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de despesas", e);
        }
    }

}
