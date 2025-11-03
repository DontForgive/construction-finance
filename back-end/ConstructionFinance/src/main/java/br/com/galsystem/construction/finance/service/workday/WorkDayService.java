package br.com.galsystem.construction.finance.service.workday;


import br.com.galsystem.construction.finance.dto.workday.WorkDayCreateDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayPaymentDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayUpdateDTO;
import br.com.galsystem.construction.finance.enums.WorkDayStatus;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.WorkDayMapper;
import br.com.galsystem.construction.finance.models.Expense;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.models.WorkDay;
import br.com.galsystem.construction.finance.repository.*;
import br.com.galsystem.construction.finance.security.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkDayService {

    private final WorkDayRepository workDayRepository;
    private final SupplierRepository supplierRepository;
    private final WorkDayMapper workDayMapper;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

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
        if (dto.workdayIds() == null || dto.workdayIds().isEmpty()) {
            throw new NotFoundException("Nenhum registro de WorkDay informado para pagamento.");
        }

        List<WorkDay> workdays = workDayRepository.findAllById(dto.workdayIds());
        if (workdays.isEmpty()) {
            throw new NotFoundException("Nenhum WorkDay encontrado para os IDs informados.");
        }

        BigDecimal total = workdays.stream()
                .map(WorkDay::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Long uid = currentUser.id();

        final User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

        Expense expense = Expense.builder()
                .user(user)
                .date(dto.paymentDate())
                .description(dto.description())
                .supplier(dto.supplierId() != null ? supplierRepository.getReferenceById(dto.supplierId()) : null)
                .category(dto.categoryId() != null ? categoryRepository.getReferenceById(dto.categoryId()) : null)
                .amount(total)
                .build();

        expenseRepository.save(expense);

        for (WorkDay workday : workdays) {
            workday.setStatus(WorkDayStatus.PAGO);
            workday.setPaymentDate(dto.paymentDate());
            workday.setExpense(expense);
            workday.setUpdatedAt(LocalDateTime.now());
            workDayRepository.save(workday);
        }
    }


}
