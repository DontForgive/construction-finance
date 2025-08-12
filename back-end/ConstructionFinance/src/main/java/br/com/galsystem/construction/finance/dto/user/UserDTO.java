package br.com.galsystem.construction.finance.dto.user;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class UserDTO {
    @NonNull private Long id;
    @NonNull private String email;
    @NonNull private String username;

    @Column(name = "full_name")
    private String fullName;
}
