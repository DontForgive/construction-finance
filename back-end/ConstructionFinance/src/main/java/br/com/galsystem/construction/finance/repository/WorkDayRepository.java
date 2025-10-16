package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {

    @Query("""
        SELECT w FROM WorkDay w
        WHERE (:supplierId IS NULL OR w.supplier.id = :supplierId)
          AND (w.date BETWEEN :startDate AND :endDate)
        ORDER BY w.date DESC
    """)
    List<WorkDay> findBySupplierAndDateRange(Long supplierId, LocalDate startDate, LocalDate endDate);
}
