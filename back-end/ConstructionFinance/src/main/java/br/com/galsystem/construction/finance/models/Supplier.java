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

    @Column(length = 255)
    private String street;

    @Column(length = 20)
    private String number;

    @Column(length = 150)
    private String complement;

    @Column(length = 100)
    private String neighborhood;

    @Column(length = 120)
    private String city;

    @Column(length = 2)
    private String state;

    @Column(length = 2)
    private String country;

    @Column(length = 255)
    private String reference;

}
