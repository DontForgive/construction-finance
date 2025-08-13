package br.com.galsystem.construction.finance.controller.payer;

import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.models.Payer;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.PayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payer")
@RequiredArgsConstructor
public class PayerController {

    private final PayerService payerService;

    @PostMapping
    public ResponseEntity<Response<PayerDTO>> create(@Valid @RequestBody PayerCreateDTO dto) {
        PayerDTO out = payerService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new Response<>(201, "Pagador criado com sucesso!", out));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<PayerDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody PayerUpdateDTO dto) {

        PayerDTO out = payerService.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Pagador atualizado com sucesso!", out));
    }

    @GetMapping
    public ResponseEntity<Response<List<PayerDTO>>> list() {
        Response<List<PayerDTO>> resp = new Response<>();

        List<PayerDTO> data = payerService.findAll()
                .stream()
                .map(p -> new PayerDTO(p.getId(), p.getName()))
                .collect(Collectors.toList());

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("Lista de pagadores obtida com sucesso.");
        resp.setData(data);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<PayerDTO>> getById(@PathVariable Long id) {
        Response<PayerDTO> resp = new Response<>();

        Optional<Payer> opt = payerService.findById(id);
        if (opt.isEmpty()) {
            resp.getErros().add("Pagador com ID " + id + " não encontrado.");
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("Recurso não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        Payer p = opt.get();

        PayerDTO out = new PayerDTO(p.getId(), p.getName());
        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("Pagador obtido com sucesso.");
        resp.setData(out);

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Integer>> delete(@PathVariable Long id) {
        Response<Integer> resp = new Response<>();

        Optional<Payer> existente = payerService.findById(id);
        if (existente.isEmpty()) {
            resp.getErros().add("Pagador com ID " + id + " não encontrado.");
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("Recurso não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        payerService.deleteById(id);

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("Pagador removido com sucesso!");
        resp.setData(1);

        return ResponseEntity.ok(resp);
    }
}
