package br.com.galsystem.construction.finance.models;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payer", uniqueConstraints = {
        @UniqueConstraint(name = "uq_payer_name", columnNames = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}