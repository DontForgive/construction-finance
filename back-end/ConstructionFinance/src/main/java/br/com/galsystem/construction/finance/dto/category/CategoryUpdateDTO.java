package br.com.galsystem.construction.finance.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String name,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description
) {}
