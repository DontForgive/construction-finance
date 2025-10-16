package br.com.galsystem.construction.finance.models;

import br.com.galsystem.construction.finance.enums.WorkDayStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_day")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyValue;

    @Column(length = 255)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WorkDayStatus status = WorkDayStatus.PENDENTE;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;
}
