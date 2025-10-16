package br.com.galsystem.construction.finance.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Entity
@Table(name = "supplier")
@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NonNull
    private String name;

    @Column(nullable = false)
    @NonNull
    @Builder.Default
    private boolean worker = false;

}
