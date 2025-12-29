package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.expense.ExpenseCreateDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    Page<ExpenseDTO> list(
            String description,
            Long supplierId,
            Long payerId,
            Long categoryId,
            String paymentMethod,
            LocalDate startDate,
            LocalDate endDate,
            Long serviceContractId,
            Pageable pageable
    );

    ExpenseDTO findById(Long id);

    ExpenseDTO create(ExpenseCreateDTO dto);

    ExpenseDTO update(Long id, ExpenseUpdateDTO dto);

    void delete(Long id);

    ExpenseDTO attachFile(Long id, MultipartFile file);

    void removeAttachment(Long id);

    List<ExpenseCreateDTO> ExpenseCreateByFileDTO(MultipartFile file);

    byte[] generateExpensesTemplate();

    byte[] generateReceipt(Long id);


}

