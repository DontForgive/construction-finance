package br.com.galsystem.construction.finance.controller.expense;

import br.com.galsystem.construction.finance.dto.expense.*;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.expense.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;


    @GetMapping
    public ResponseEntity<Response<Page<ExpenseDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String dir
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));

        Page<ExpenseDTO> result = service.list(pageable);

        Response<Page<ExpenseDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de despesas");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<ExpenseDTO>> findById(@PathVariable Long id) {
        ExpenseDTO dto = service.findById(id);
        return ResponseEntity.ok(new Response<>(200, "Despesa encontrada", dto));
    }

    @PostMapping
    public ResponseEntity<Response<ExpenseDTO>> create(@Valid @RequestBody ExpenseCreateDTO dto) {
        ExpenseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(201, "Despesa criada com sucesso", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<ExpenseDTO>> update(@PathVariable Long id,
                                                       @Valid @RequestBody ExpenseUpdateDTO dto) {
        ExpenseDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Despesa atualizada com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new Response<>(200, "Despesa removida com sucesso", null));
    }

    @PutMapping(value = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<ExpenseDTO>> attach(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        ExpenseDTO updated = service.attachFile(id, file);
        return ResponseEntity.ok(new Response<>(200, "Anexo adicionado/atualizado", updated));
    }

    @DeleteMapping("/{id}/attachment")
    public ResponseEntity<Response<Void>> removeAttachment(@PathVariable Long id) {
        service.removeAttachment(id);
        return ResponseEntity.ok(new Response<>(200, "Anexo removido", null));
    }


}
