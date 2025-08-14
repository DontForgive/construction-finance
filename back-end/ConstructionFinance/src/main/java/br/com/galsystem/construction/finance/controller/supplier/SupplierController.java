package br.com.galsystem.construction.finance.controller.supplier;


import br.com.galsystem.construction.finance.dto.category.CategoryCreateDTO;
import br.com.galsystem.construction.finance.dto.category.CategoryDTO;
import br.com.galsystem.construction.finance.dto.category.CategoryUpdateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.supplier.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    @GetMapping
    public ResponseEntity<Response<Page<SupplierDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String dir
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));

        Page<SupplierDTO> result = service.listar(pageable);

        Response<Page<SupplierDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de Fornecedores");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<SupplierDTO>> findById(@PathVariable Long id) {
        SupplierDTO dto = service.findById(id);
        return ResponseEntity.ok(new Response<>(200, "Fornecedor encontrado", dto));
    }

    @PostMapping
    public ResponseEntity<Response<SupplierDTO>> create(@Valid @RequestBody SupplierCreateDTO dto) {
        SupplierDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(201, "Fornecedor criado com sucesso", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<SupplierDTO>> update(@PathVariable Long id,
                                                        @Valid @RequestBody SupplierUpdateDTO dto) {
        SupplierDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Fornecedor atualizado com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new Response<>(200, "Fornecedor removido com sucesso", null));
    }
}
