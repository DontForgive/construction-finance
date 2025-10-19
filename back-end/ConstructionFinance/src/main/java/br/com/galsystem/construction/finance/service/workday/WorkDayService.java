package br.com.galsystem.construction.finance.service.workday;


import br.com.galsystem.construction.finance.dto.workday.WorkDayCreateDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayPaymentDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayUpdateDTO;
import br.com.galsystem.construction.finance.enums.WorkDayStatus;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.mapper.WorkDayMapper;
import br.com.galsystem.construction.finance.models.Expense;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.models.WorkDay;
import br.com.galsystem.construction.finance.repository.CategoryRepository;
import br.com.galsystem.construction.finance.repository.ExpenseRepository;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import br.com.galsystem.construction.finance.repository.WorkDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkDayService {

    private final WorkDayRepository workDayRepository;
    private final SupplierRepository supplierRepository;
    private final WorkDayMapper workDayMapper;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public WorkDayDTO create(WorkDayCreateDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        WorkDay entity = workDayMapper.toEntity(dto);
        entity.setSupplier(supplier);
        entity.setStatus(WorkDayStatus.PENDENTE);

        workDayRepository.save(entity);

        return workDayMapper.toDTO(entity);
    }

    @Transactional
    public WorkDayDTO update(Long id, WorkDayUpdateDTO dto) {
        WorkDay entity = workDayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        workDayMapper.updateEntity(entity, dto);
        entity.setUpdatedAt(java.time.LocalDateTime.now());

        workDayRepository.save(entity);
        return workDayMapper.toDTO(entity);
    }

    public List<WorkDayDTO> findByMonth(int year, int month, Long supplierId) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return workDayRepository.findBySupplierAndDateRange(supplierId, start, end)
                .stream()
                .map(workDayMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        if (!workDayRepository.existsById(id)) {
            throw new RuntimeException("Registro não encontrado");
        }
        workDayRepository.deleteById(id);
    }

    @Transactional
    public void registerPayment(WorkDayPaymentDTO dto) {
        var workDays = workDayRepository.findAllById(dto.workDayIds());
        if (workDays.isEmpty()) throw new NotFoundException("Nenhum registro encontrado para pagamento.");

        var total = workDays.stream()
                .map(WorkDay::getDailyValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        workDays.forEach(w -> w.setStatus(WorkDayStatus.PAGO));
        workDayRepository.saveAll(workDays);

        var supplier = supplierRepository.findById(dto.payToSupplierId())
                .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado"));

        var category = categoryRepository.findById(dto.categoryId())
                .orElse(null);

        var expense = Expense.builder()
                .description(dto.description())
                .amount(total)
                .date(dto.paymentDate())
                .supplier(supplier)
                .category(category)
                .build();

        expenseRepository.save(expense);
    }

}
