package br.com.galsystem.construction.finance.dto.payer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PayerUpdateDTO (
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
                String name
){}
