package br.com.galsystem.construction.finance.security.auth;

import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    /** Retorna o username vindo do token (subject). */
    public String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResourceNotFoundException("Usuário não autenticado");
        }
        return auth.getName(); // JwtAuthFilter já coloca o username como principal
    }

    /** Resolve e retorna o ID do usuário autenticado. */
    public Long id() {
        String username = username();
        return userRepository.findIdByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
    }
}
