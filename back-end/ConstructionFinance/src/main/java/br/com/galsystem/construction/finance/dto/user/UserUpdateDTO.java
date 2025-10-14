package br.com.galsystem.construction.finance.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UserUpdateDTO(
        @NotBlank(message = "Username é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        @Size(min = 2, message = "O nome deve ter no minimo 2 caracteres")
        String username,
        
        String fullName,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        String email,
        String phoneNumber
) {
}
