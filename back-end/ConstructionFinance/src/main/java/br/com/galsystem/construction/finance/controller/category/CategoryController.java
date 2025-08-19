package br.com.galsystem.construction.finance.controller.category;
import br.com.galsystem.construction.finance.dto.category.*;

import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping()
    public ResponseEntity<Response<Page<CategoryDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String dir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));

        Page<CategoryDTO> result = service.list(name, description, pageable);

        Response<Page<CategoryDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de categorias");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<CategoryDTO>> findById(@PathVariable Long id) {
        CategoryDTO dto = service.findById(id);
        return ResponseEntity.ok(new Response<>(200, "Categoria encontrada", dto));
    }

    @PostMapping("  ")
    public ResponseEntity<Response<CategoryDTO>> create(@Valid @RequestBody CategoryCreateDTO dto) {
        CategoryDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(201, "Categoria criada com sucesso", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<CategoryDTO>> update(@PathVariable Long id,
                                                       @Valid @RequestBody CategoryUpdateDTO dto) {
        CategoryDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Categoria atualizada com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new Response<>(200, "Categoria removida com sucesso", null));
    }
}
