package br.com.galsystem.construction.finance.dto.supplier;

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
        @Size(max = 150, message = "Rua deve ter no máximo 150 caracteres")
        String rua,
        @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
        String numero,
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String bairro,
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,
        @Size(max = 2, message = "Estado deve ser a sigla de 2 letras da UF")
        @Pattern(regexp = "(?i)^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)?$",
                message = "Estado inválido")
        String estado,
        @Size(max = 9, message = "CEP deve ter no máximo 9 caracteres")
        @Pattern(regexp = "^(\\d{5}-?\\d{3})?$", message = "CEP deve ter 8 dígitos, com ou sem hífen")
        String cep,
        @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
        String complemento
) {
}
