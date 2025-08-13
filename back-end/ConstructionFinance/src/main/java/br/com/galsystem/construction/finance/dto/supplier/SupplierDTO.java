package br.com.galsystem.construction.finance.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {

    private Long id;
    @NotBlank(message = "Nome do Fornecedor é obrigatório")
    private String name;

}
