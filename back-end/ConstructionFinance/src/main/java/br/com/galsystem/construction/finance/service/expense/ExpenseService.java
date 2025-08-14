package br.com.galsystem.construction.finance.service.expense;
import br.com.galsystem.construction.finance.dto.expense.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {
    Page<ExpenseDTO> list(Pageable pageable);
    ExpenseDTO findById(Long id);
    ExpenseDTO create(ExpenseCreateDTO dto);
    ExpenseDTO update(Long id, ExpenseUpdateDTO dto);
    void delete(Long id);
}
