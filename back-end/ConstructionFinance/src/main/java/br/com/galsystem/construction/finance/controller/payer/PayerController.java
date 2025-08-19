package br.com.galsystem.construction.finance.controller.payer;
import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.payer.PayerService;
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

@Tag(name = "Payer", description = "Pagador de despesas")
@RestController
@RequestMapping("/payer")
@RequiredArgsConstructor
public class PayerController {

    private final PayerService service;

    @GetMapping
    public ResponseEntity<Response<Page<PayerDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String dir

    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));

        Page<PayerDTO> result = service.listar(pageable);

        Response<Page<PayerDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de pagadores");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<PayerDTO>> findById(@PathVariable Long id) {
        PayerDTO dto = service.findById(id);
        return ResponseEntity.ok(new Response<>(200, "Pagador encontrado", dto));
    }

    @PostMapping
    public ResponseEntity<Response<PayerDTO>> save(@RequestBody @Valid PayerCreateDTO dto) {
        PayerDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(201, "Pagador cadastrado com sucesso", created));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<PayerDTO>> update(@PathVariable Long id, @Valid @RequestBody PayerUpdateDTO dto) {
        PayerDTO updated = service.update(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(200, "Pagador atualizado com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.ok(new Response<>(200, "Pagador removido com sucesso", null));
    }

}
