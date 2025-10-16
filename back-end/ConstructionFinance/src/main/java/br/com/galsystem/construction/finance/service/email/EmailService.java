package br.com.galsystem.construction.finance.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("gabriel.arruda.lima@galsystems.com.br");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String resetUrl) {
        String subject = "Redefinição de Senha";
        String body = """
                Olá!

                Recebemos uma solicitação para redefinir sua senha.

                Clique no link abaixo para criar uma nova senha:
                %s

                Se você não solicitou esta alteração, ignore este e-mail.

                Atenciosamente,
                Equipe de Suporte
                """.formatted(resetUrl);

        sendEmail(to, subject, body);
    }
}
