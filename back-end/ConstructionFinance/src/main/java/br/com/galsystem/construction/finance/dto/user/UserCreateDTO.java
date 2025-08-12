package br.com.galsystem.construction.finance.dto.user;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    @NotBlank(message = "O campo 'username' é obrigatório e não pode estar em branco.")
    private String username;

    @NotBlank(message = "O campo 'email' é obrigatório e deve conter um endereço de e-mail válido.")
    @Email
    private String email;

    @NotBlank(message = "O campo 'password' é obrigatório e não pode estar em branco.")
    @Size(min = 8, message = "A senha deve ter no mínimo {min} caracteres.")
    private String password;

    @Column(name="full_name")
    private String fullName;
}
