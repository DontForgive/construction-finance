package br.com.galsystem.construction.finance.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, false);
    }

    public void sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            helper.setFrom("gabriel.arruda.lima@galsystems.com.br");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }

    public void sendPasswordResetEmail(String to, String resetUrl) {
        String subject = "Redefinição de Senha - Construction Finance";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;">
                    <div style="background-color: #2c3e50; color: white; padding: 20px; text-align: center;">
                        <h1>Construction Finance</h1>
                    </div>
                    <div style="padding: 20px; color: #333;">
                        <h2>Olá!</h2>
                        <p>Recebemos uma solicitação para redefinir a sua senha. Clique no botão abaixo para prosseguir:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">Redefinir Senha</a>
                        </div>
                        <p>Se você não consegue clicar no botão, copie e cole o link abaixo no seu navegador:</p>
                        <p style="word-break: break-all; color: #3498db;">%s</p>
                        <p>Este link expirará em 2 horas.</p>
                        <p>Se você não solicitou esta alteração, ignore este e-mail por motivos de segurança.</p>
                    </div>
                    <div style="background-color: #f9f9f9; color: #7f8c8d; padding: 15px; text-align: center; font-size: 12px;">
                        &copy; 2026 Construction Finance. Todos os direitos reservados.
                    </div>
                </div>
                """.formatted(resetUrl, resetUrl);

        sendEmail(to, subject, htmlContent, true);
    }
}
