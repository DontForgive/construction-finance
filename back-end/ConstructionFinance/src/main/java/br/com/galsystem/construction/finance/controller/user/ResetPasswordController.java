package br.com.galsystem.construction.finance.controller.user;

import br.com.galsystem.construction.finance.dto.user.ResetPasswordRequest;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.user.ResetPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/auth")
public class ResetPasswordController {
    private final ResetPasswordService resetPasswordService;

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "resetPassword";
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        Response<Void> response = new Response<>();

        try {
            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                response.setStatus(400);
                response.setMessage("As senhas não coincidem.");
                return ResponseEntity.badRequest().body(response);
            }

            resetPasswordService.resetPassword(request.getToken(), request.getNewPassword());

            response.setStatus(200);
            response.setMessage("Senha redefinida com sucesso!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(400);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
