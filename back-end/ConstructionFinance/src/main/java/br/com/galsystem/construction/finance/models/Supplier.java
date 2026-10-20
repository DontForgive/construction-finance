package br.com.galsystem.construction.finance.models;

import jakarta.persistence.*;
import lombok.*;

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

    @Builder.Default
    private Boolean worker = false;

    private String keyPix;

    @Column(name = "street", length = 150)
    private String rua;

    @Column(name = "number", length = 10)
    private String numero;

    @Column(name = "neighborhood", length = 100)
    private String bairro;

    @Column(name = "city", length = 100)
    private String cidade;

    @Column(name = "state", length = 2)
    private String estado;

    @Column(name = "zip_code", length = 8)
    private String cep;

    @Column(name = "complement", length = 100)
    private String complemento;

}
