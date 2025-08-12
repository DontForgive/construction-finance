package br.com.galsystem.construction.finance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class UserDTO {
    @NonNull private Long id;
    @NonNull private String email;
    @NonNull private String username;
    private String fullName;
}
