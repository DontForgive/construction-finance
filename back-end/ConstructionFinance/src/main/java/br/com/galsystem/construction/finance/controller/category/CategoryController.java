package br.com.galsystem.construction.finance.controller.category;

import br.com.galsystem.construction.finance.dto.category.CategoryCreateDTO;
import br.com.galsystem.construction.finance.dto.category.CategoryDTO;
import br.com.galsystem.construction.finance.models.Category;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Response<CategoryDTO>> create(@Valid @RequestBody CategoryCreateDTO dto) {
        CategoryDTO out = categoryService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new Response<>(201, "Categoria criado com sucesso!", out));
    }

    @GetMapping
    public ResponseEntity<Response<List<CategoryDTO>>> listAll() {
        List<Category> list = categoryService.findAll();
        List<CategoryDTO> out = new ArrayList<>(list.size());
        for (Category c : list) {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setDescription(c.getDescription());
            out.add(dto);
        }

        Response<List<CategoryDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de categorias");
        resp.setData(out);
        return ResponseEntity.ok(resp);
    }

    // GET /categories/{id} - buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Response<CategoryDTO>> getById(@PathVariable Long id) {
        Optional<Category> opt = categoryService.findById(id);
        Response<CategoryDTO> resp = new Response<>();

        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.getErros().add("Categoria não encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        Category c = opt.get();
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());

        resp.setStatus(200);
        resp.setMessage("Categoria encontrada");
        resp.setData(dto);
        return ResponseEntity.ok(resp);
    }

    // PUT /categories/{id} - atualizar
    @PutMapping("/{id}")
    public ResponseEntity<Response<CategoryDTO>> update(@PathVariable Long id,
                                                        @Valid @RequestBody CategoryCreateDTO dto) {
        Optional<Category> opt = categoryService.findById(id);
        Response<CategoryDTO> resp = new Response<>();

        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.getErros().add("Categoria não encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        Category c = opt.get();
        c.setName(dto.getName().trim());
        c.setDescription(dto.getDescription());

        Category saved = categoryService.save(c);

        CategoryDTO out = new CategoryDTO();
        out.setId(saved.getId());
        out.setName(saved.getName());
        out.setDescription(saved.getDescription());

        resp.setStatus(200);
        resp.setMessage("Categoria atualizada com sucesso!");
        resp.setData(out);
        return ResponseEntity.ok(resp);
    }

    // DELETE /categories/{id} - deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // (opcional) validar existência antes
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
