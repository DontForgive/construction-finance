package br.com.galsystem.construction.finance.controller.user;

import br.com.galsystem.construction.finance.dto.user.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.user.UserDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.auth.AuthService;
import br.com.galsystem.construction.finance.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Login")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<UserDTO>> register(@Valid @RequestBody UserCreateDTO dto) {
        Response<UserDTO> resp = userService.save(dto);

        // Usa o status do próprio Response se vier setado; senão, deduz (201/409)
        int code = resp.getStatus() == 0
                ? (resp.getErros().isEmpty() ? 201 : 409)
                : resp.getStatus();

        return ResponseEntity.status(code).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO body) {
        return authService.login(body.getUsername(), body.getPassword())
                .map(t -> ResponseEntity.ok(Map.of("token", t)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuário ou senha inválidos")));
    }


    // DTO simples para login
    public static class LoginRequestDTO {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
