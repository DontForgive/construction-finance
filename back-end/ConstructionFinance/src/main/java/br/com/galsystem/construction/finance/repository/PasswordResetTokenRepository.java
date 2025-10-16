package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.PasswordResetToken;
import br.com.galsystem.construction.finance.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
