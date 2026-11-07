package br.com.galsystem.construction.finance.dto.supplier;

import br.com.galsystem.construction.finance.validation.IsoCountry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierCreateDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        @Size(min = 2, message = "O nome deve ter no minimo 2 caracteres")
        String name,
        @NotNull(message = "Trabalhador não pode ser nulo ")
        Boolean worker,
        String keyPix,
        @Pattern(regexp = "^\\d{8}$|^$", message = "CEP deve conter 8 dígitos")
        String zipCode,
        @Size(max = 255, message = "Rua deve ter no máximo 255 caracteres")
        String street,
        @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
        String number,
        @Size(max = 150, message = "Complemento deve ter no máximo 150 caracteres")
        String complement,
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String neighborhood,
        @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
        String city,
        @Pattern(regexp = "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)?$",
                message = "UF deve ser uma das 27 siglas válidas")
        String state,
        @IsoCountry
        String country,
        @Size(max = 255, message = "Referência deve ter no máximo 255 caracteres")
        String reference
) {
}
