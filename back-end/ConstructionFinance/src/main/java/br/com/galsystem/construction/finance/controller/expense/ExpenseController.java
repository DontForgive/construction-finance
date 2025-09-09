package br.com.galsystem.construction.finance.controller.expense;

import br.com.galsystem.construction.finance.dto.expense.ExpenseCreateDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseUpdateDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.expense.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Expenses", description = "Operações de despesas")
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;


    @GetMapping
    public ResponseEntity<Response<Page<ExpenseDTO>>> list(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "id") final String sort,
            @RequestParam(defaultValue = "DESC") final String dir,
            @RequestParam(required = false) final String description,
            @RequestParam(required = false) final Long supplierId,
            @RequestParam(required = false) final Long payerId,
            @RequestParam(required = false) final Long categoryId,
            @RequestParam(required = false) final String paymentMethod,
            @RequestParam(required = false) final LocalDate startDate,
            @RequestParam(required = false) final LocalDate endDate
    ) {
        final int safePage = Math.max(page, 0);
        final int safeSize = Math.min(Math.max(size, 1), 100);

        final Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        final Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));


        final Page<ExpenseDTO> result = service.list(
                description, supplierId, payerId, categoryId,
                paymentMethod, startDate, endDate, pageable
        );

        final Response<Page<ExpenseDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de despesas");
        resp.setData(result);

        return ResponseEntity.ok(resp);
    }


    @GetMapping("/data")
    public ResponseEntity<?> list(
            @RequestParam(required = false) final LocalDate startDate,
            @RequestParam(required = false) final LocalDate endDate
    ) {
        // ✅ Aqui já chega convertido automaticamente
        return ResponseEntity.ok("Start: " + startDate + " | End: " + endDate);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Response<ExpenseDTO>> findById(@PathVariable final Long id) {
        final ExpenseDTO dto = service.findById(id);
        return ResponseEntity.ok(new Response<>(200, "Despesa encontrada", dto, null));
    }

    @Operation(summary = "Criar despesa")
    @PostMapping
    public ResponseEntity<Response<ExpenseDTO>> create(@Valid @RequestBody final ExpenseCreateDTO dto) {
        final ExpenseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(201, "Despesa criada com sucesso", created, null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<ExpenseDTO>> update(@PathVariable final Long id,
                                                       @Valid @RequestBody final ExpenseUpdateDTO dto) {
        final ExpenseDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Despesa atualizada com sucesso", updated, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable final Long id) {
        service.delete(id);
        return ResponseEntity.ok(new Response<>(200, "Despesa removida com sucesso", null, null));
    }

    @PutMapping(value = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<ExpenseDTO>> attach(
            @PathVariable final Long id,
            @RequestPart("file") final MultipartFile file
    ) {
        final ExpenseDTO updated = service.attachFile(id, file);
        return ResponseEntity.ok(new Response<>(200, "Anexo adicionado/atualizado", updated, null));
    }

    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<List<ExpenseCreateDTO>>> createByUploadFile(
            @RequestPart("file") final MultipartFile file
    ) {
        final List<ExpenseCreateDTO > updated = service.ExpenseCreateByFileDTO(file);


        return ResponseEntity.ok(new Response<>(200, "Cadastros realizados com sucesso!", updated, null));
    }


    @DeleteMapping("/{id}/attachment")
    public ResponseEntity<Response<Void>> removeAttachment(@PathVariable final Long id) {
        service.removeAttachment(id);
        return ResponseEntity.ok(new Response<>(200, "Anexo removido", null, null));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] bytes = service.generateExpensesTemplate();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template-despesas.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

}
