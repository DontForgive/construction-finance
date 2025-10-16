package br.com.galsystem.construction.finance.controller.user;

import br.com.galsystem.construction.finance.dto.user.LoginRequestDTO;
import br.com.galsystem.construction.finance.dto.user.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.user.UserDTO;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.auth.AuthService;
import br.com.galsystem.construction.finance.service.user.ResetPasswordService;
import br.com.galsystem.construction.finance.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Auth", description = "Login")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<Void>> processForgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Response<Void> response = new Response<>();

        try {
            if (email == null || email.isBlank()) {
                response.setStatus(400);
                response.setMessage("E-mail não informado.");
                return ResponseEntity.badRequest().body(response);
            }
            resetPasswordService.createPasswordResetToken(email);
            response.setStatus(200);
            response.setMessage("Um link de redefinição foi enviado para o e-mail informado.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(400);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
