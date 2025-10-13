package br.com.galsystem.construction.finance.response;

import lombok.*;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class PasswordUpdateResponse {
    private int code;
    private String message;
}
