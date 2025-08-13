package br.com.galsystem.construction.finance.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateDTO {
    @NotBlank(message = "Nome da categoria é obrigatório.")
    private String name;
    private String description;
}
