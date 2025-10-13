package br.com.galsystem.construction.finance.dto.user;

public class UpdatePasswordRequest {
    private String password;
    private String newPassword;
    private String confirmNewPassword;

    // Getters e Setters
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
