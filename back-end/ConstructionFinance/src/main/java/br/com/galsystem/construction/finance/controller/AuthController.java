package br.com.galsystem.construction.finance.controller;

import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User savedUser = authService.register(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        Optional<String> token = authService.login(loginRequest.get("username"), loginRequest.get("password"));

        if (token.isPresent()) {
            return ResponseEntity.ok(Map.of("token", token.get()));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Usuário ou senha inválidos"));
        }
    }
}
