package br.com.galsystem.construction.finance.service.user;

import br.com.galsystem.construction.finance.models.PasswordResetToken;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.repository.PasswordResetTokenRepository;
import br.com.galsystem.construction.finance.repository.UserRepository;
import br.com.galsystem.construction.finance.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(2));

        tokenRepository.save(resetToken);

        String resetUrl = "https://seusite.com/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou inexistente."));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("O token de redefinição expirou. Solicite um novo link.");
        }

        User user = resetToken.getUser();

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);

        userRepository.save(user);

        tokenRepository.delete(resetToken);

         emailService.sendEmail(user.getEmail(), "Senha alterada com sucesso", "Sua senha foi redefinida com sucesso.");
    }
}

