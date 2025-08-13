package br.com.galsystem.construction.finance.dto.payer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayerCreateDTO {
    @NotBlank(message = "Nome do Pagador é obrigatório")
    private String name;
}
