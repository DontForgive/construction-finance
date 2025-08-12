package br.com.galsystem.construction.finance.controller;

import br.com.galsystem.construction.finance.dto.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.UserDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.AuthService;
import br.com.galsystem.construction.finance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO body) {
        Optional<String> token = authService.login(body.getUsername(), body.getPassword());

        if (token.isPresent()) {
            return ResponseEntity.ok(Map.of("token", token.get()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Usuário ou senha inválidos"));
    }

    // DTO simples para login
    public static class LoginRequestDTO {
        @jakarta.validation.constraints.NotBlank
        private String username;
        @jakarta.validation.constraints.NotBlank
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
