package br.com.galsystem.construction.finance.controller.serviceContract;

import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractCreateDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractFilterDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractUpdateDTO;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.serviceContract.serviceContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "ServiceContract", description = "ServiceContract")
@RestController
@RequestMapping("/serviceContract")
@RequiredArgsConstructor
public class serviceContractController {

    private final serviceContract service;

    // ------------------------------
    // LISTAR COM FILTROS + PAGINAÇÃO
    // ------------------------------
    @GetMapping
    public ResponseEntity<Response<Page<ServiceContractDTO>>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        ServiceContractFilterDTO filters = ServiceContractFilterDTO.builder()
                .name(Optional.ofNullable(name))
                .description(Optional.ofNullable(description))
                .supplierId(Optional.ofNullable(supplierId))
                .categoryId(Optional.ofNullable(categoryId))
                .build();
        final Page<ServiceContractDTO> result = service.findByFilters(filters, pageable);

        final Response<Page<ServiceContractDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de Contratos de Serviço");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    // ------------------------------
    // LISTA SEM FILTRO (PAGINADA)
    // ------------------------------
    @GetMapping("/all")
    public Page<ServiceContractDTO> listAll(Pageable pageable) {
        return service.listAll(pageable);
    }

    // ------------------------------
    // BUSCAR POR ID
    // ------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ServiceContractDTO> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço com ID %d não encontrado".formatted(id)));
    }

    // ------------------------------
    // CRIAR
    // ------------------------------
    @Operation(summary = "Criar Contrato de Serviço")
    @PostMapping
    public ResponseEntity<Response<ServiceContractDTO>> create(@RequestBody @Valid ServiceContractCreateDTO dto) {
        ServiceContractDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new Response<>(201, "Contrato de Serviço Criado com Sucesso", saved, null));

    }

    // ------------------------------
    // ATUALIZAR
    // ------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<Response<ServiceContractDTO>> update(
            @PathVariable Long id,
            @RequestBody @Valid ServiceContractUpdateDTO dto
    ) {
        final ServiceContractDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Contrato de Serviço Atualizado com Sucesso", updated, null));
    }

    // ------------------------------
    // EXCLUIR
    // ------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
