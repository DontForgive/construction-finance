package br.com.galsystem.construction.finance.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupplierCreateDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        @Size(min = 2, message = "O nome deve ter no minimo 2 caracteres")
        String name,
        
        @NotNull(message = "Trabalhador não pode ser nulo ")
        Boolean worker
) {
}
