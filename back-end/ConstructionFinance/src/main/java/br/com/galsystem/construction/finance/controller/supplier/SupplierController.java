package br.com.galsystem.construction.finance.controller.supplier;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.supplier.SupplierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Supplier", description = "Fornecedor")
@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    @GetMapping
    public ResponseEntity<Response<Page<SupplierDTO>>> list(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "id") final String sort,
            @RequestParam(defaultValue = "ASC") final String dir,
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final Boolean worker
    ) {
        final int safePage = Math.max(page, 0);
        final int safeSize = Math.min(Math.max(size, 1), 100);
        final Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        final Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));

        final Page<SupplierDTO> result = service.listar(name, worker, pageable);

        final Response<Page<SupplierDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de Fornecedores");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<SupplierDTO>> findById(@PathVariable final Long id) {
        final SupplierDTO dto = service.findById(id);
        return ResponseEntity.ok(new Response<>(200, "Fornecedor encontrado", dto, null));
    }

    @PostMapping
    public ResponseEntity<Response<SupplierDTO>> create(@Valid @RequestBody final SupplierCreateDTO dto) {
        final SupplierDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(201, "Fornecedor criado com sucesso", created, null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<SupplierDTO>> update(@PathVariable final Long id,
                                                        @Valid @RequestBody final SupplierUpdateDTO dto) {
        final SupplierDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Fornecedor atualizado com sucesso", updated, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable final Long id) {
        service.delete(id);
        return ResponseEntity.ok(new Response<>(200, "Fornecedor removido com sucesso", null, null));
    }
}
