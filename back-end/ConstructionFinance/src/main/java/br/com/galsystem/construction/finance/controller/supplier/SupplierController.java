package br.com.galsystem.construction.finance.controller.supplier;

import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.supplier.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<Response<SupplierDTO>> create(@Valid @RequestBody SupplierDTO dto) {
        SupplierDTO out = supplierService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new Response<>(201, "Fornecedor criado com sucesso!", out));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<SupplierDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierDTO dto) {

        SupplierDTO out = supplierService.update(id, dto);
        return ResponseEntity.ok(new Response<>(200, "Fornecedor atualizado com sucesso!", out));
    }


}
