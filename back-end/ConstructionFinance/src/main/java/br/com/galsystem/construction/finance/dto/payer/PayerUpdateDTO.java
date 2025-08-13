package br.com.galsystem.construction.finance.dto.payer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayerUpdateDTO {
    @NotBlank(message = "Nome é obrigatório.")
    private String name;
    // getters/setters
}