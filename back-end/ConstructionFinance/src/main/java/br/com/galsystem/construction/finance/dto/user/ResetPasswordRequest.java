package br.com.galsystem.construction.finance.dto.user;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String token;
    private String newPassword;
    private String confirmNewPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
