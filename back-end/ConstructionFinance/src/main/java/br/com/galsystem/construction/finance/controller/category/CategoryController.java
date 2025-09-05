package br.com.galsystem.construction.finance.controller.category;

import br.com.galsystem.construction.finance.dto.category.CategoryCreateDTO;
import br.com.galsystem.construction.finance.dto.category.CategoryDTO;
import br.com.galsystem.construction.finance.dto.category.CategoryUpdateDTO;
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

    @GetMapping
    public ResponseEntity<Response<Page<CategoryDTO>>> list(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "id") final String sort,
            @RequestParam(defaultValue = "ASC") final String dir,
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final String description
    ) {
        final int safePage = Math.max(page, 0);
        final int safeSize = Math.min(Math.max(size, 1), 100);
        final Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        final Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));

        final Page<CategoryDTO> result = service.list(name, description, pageable);

        final Response<Page<CategoryDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de categorias");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<CategoryDTO>> findById(@PathVariable final Long id) {
        final CategoryDTO dto = service.findById(id);
        return ResponseEntity.ok(new Response<>(200, "Categoria encontrada", dto, null));
    }

    @PostMapping
    public ResponseEntity<Response<CategoryDTO>> create(@Valid @RequestBody final CategoryCreateDTO dto) {
        final CategoryDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(201, "Categoria criada com sucesso", created, null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<CategoryDTO>> update(@PathVariable final Long id,
                                                        @Valid @RequestBody final CategoryUpdateDTO dto) {
        final CategoryDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Categoria atualizada com sucesso", updated, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable final Long id) {
        service.delete(id);
        return ResponseEntity.ok(new Response<>(200, "Categoria removida com sucesso", null, null));
    }
}
