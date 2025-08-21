package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.Expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
                SELECT e FROM Expense e
                WHERE (:description IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', :description, '%')))
                  AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
                  AND (:payerId IS NULL OR e.payer.id = :payerId)
                  AND (:categoryId IS NULL OR e.category.id = :categoryId)
                  AND ((:paymentMethod IS NULL OR :paymentMethod = '') OR e.paymentMethod = :paymentMethod)
                    AND (e.date >= COALESCE(:startDate, e.date))
                   AND (e.date <= COALESCE(:endDate, e.date))
            """)
    Page<Expense> findByFilters(
            @Param("description") String description,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("categoryId") Long categoryId,
            @Param("paymentMethod") String paymentMethod,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Total por categoria
    @Query("SELECT e.category.name, SUM(e.amount) " +
            "FROM Expense e " +
            "GROUP BY e.category.name")
    List<Object[]> getTotalByCategory();

    // Total por mês
    @Query("SELECT FUNCTION('MONTH', e.date), SUM(e.amount) " +
            "FROM Expense e " +
            "GROUP BY FUNCTION('MONTH', e.date) " +
            "ORDER BY FUNCTION('MONTH', e.date)")
    List<Object[]> getTotalByMonth();

    // Total por fornecedor
    @Query("SELECT e.supplier.name, SUM(e.amount) " +
            "FROM Expense e " +
            "GROUP BY e.supplier.name")
    List<Object[]> getTotalBySupplier();

    // Total por forma de pagamento
    @Query("SELECT e.paymentMethod, SUM(e.amount) " +
            "FROM Expense e " +
            "GROUP BY e.paymentMethod")
    List<Object[]> getTotalByPaymentMethod();

    // Total por pagador
    @Query("SELECT e.payer.name, SUM(e.amount) " +
            "FROM Expense e " +
            "GROUP BY e.payer.name")
    List<Object[]> getTotalByPayer();

}
