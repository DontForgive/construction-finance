package br.com.galsystem.construction.finance.dto.supplier;

import br.com.galsystem.construction.finance.utils.AddressNormalizer;
import br.com.galsystem.construction.finance.validation.PaisValido;
import br.com.galsystem.construction.finance.validation.UfValida;
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
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos numéricos")
        String zipCode,
        @Size(max = 255, message = "Logradouro deve ter no máximo 255 caracteres")
        String street,
        @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
        String number,
        @Size(max = 150, message = "Complemento deve ter no máximo 150 caracteres")
        String complement,
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String neighborhood,
        @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
        String city,
        @UfValida(message = "UF inválida: informe uma das 27 unidades federativas (2 letras)")
        String state,
        @PaisValido(message = "País inválido: informe um código ISO 3166-1 alpha-2 (2 letras)")
        String country,
        @Size(max = 255, message = "Referência deve ter no máximo 255 caracteres")
        String reference
) {
    /**
     * Normaliza o endereço antes da validação: trim, vazio vira null, CEP sem máscara,
     * UF e país em maiúsculas.
     */
    public SupplierCreateDTO {
        zipCode = AddressNormalizer.zipCode(zipCode);
        street = AddressNormalizer.text(street);
        number = AddressNormalizer.text(number);
        complement = AddressNormalizer.text(complement);
        neighborhood = AddressNormalizer.text(neighborhood);
        city = AddressNormalizer.text(city);
        state = AddressNormalizer.upper(state);
        country = AddressNormalizer.upper(country);
        reference = AddressNormalizer.text(reference);
    }
}
