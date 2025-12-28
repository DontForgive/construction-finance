package br.com.galsystem.construction.finance.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ServiceContract")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contracted_value", precision = 12, scale = 2, nullable = false)
    private BigDecimal contractedValue;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "serviceContract")
    private List<Expense> payments = new ArrayList<>();


    public BigDecimal getTotalPaid() {
        return payments.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBalance() {
        if (contractedValue == null) return BigDecimal.ZERO;
        return contractedValue.subtract(getTotalPaid());
    }
}
