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

    @Column(name = "zip_code", length = 8)
    private String zipCode;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "number", length = 20)
    private String number;

    @Column(name = "complement", length = 150)
    private String complement;

    @Column(name = "neighborhood", length = 100)
    private String neighborhood;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "state", length = 2)
    private String state;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "reference", length = 255)
    private String reference;

}
