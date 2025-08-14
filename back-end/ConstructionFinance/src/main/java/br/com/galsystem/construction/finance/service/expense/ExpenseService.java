package br.com.galsystem.construction.finance.service.expense;
import br.com.galsystem.construction.finance.dto.expense.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ExpenseService {
    Page<ExpenseDTO> list(Pageable pageable);
    ExpenseDTO findById(Long id);
    ExpenseDTO create(ExpenseCreateDTO dto);
    ExpenseDTO update(Long id, ExpenseUpdateDTO dto);
    void delete(Long id);
    ExpenseDTO attachFile(Long id, MultipartFile file);
    void removeAttachment(Long id);

}
