package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.Expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
        SELECT e FROM Expense e
        WHERE (:description IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', :description, '%')))
          AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
          AND (:payerId IS NULL OR e.payer.id = :payerId)
          AND (:categoryId IS NULL OR e.category.id = :categoryId)
          AND ((:paymentMethod IS NULL OR :paymentMethod = '') OR e.paymentMethod = :paymentMethod)
          AND (:date IS NULL OR e.date = :date)
    """)
    Page<Expense> findByFilters(
            @Param("description") String description,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("categoryId") Long categoryId,
            @Param("paymentMethod") String paymentMethod,
            @Param("date") LocalDate date,
            Pageable pageable
    );
}
