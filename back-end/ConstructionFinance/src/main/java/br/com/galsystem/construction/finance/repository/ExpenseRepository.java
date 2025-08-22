package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.dto.expense.ChartDataDTO;
import br.com.galsystem.construction.finance.models.Expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    // --- Por Categoria
    @Query("""
        SELECT c.name AS label, SUM(e.amount) AS value
        FROM Expense e JOIN e.category c
        WHERE e.date >= COALESCE(:start, e.date)
          AND e.date <= COALESCE(:end, e.date)
          AND (:userId IS NULL OR e.user.id = :userId)
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
          AND (:payerId IS NULL OR e.payer.id = :payerId)
        GROUP BY c.name
        ORDER BY SUM(e.amount) DESC
    """)
    List<ChartDataDTO> getTotalByCategory(LocalDate start,
                                          LocalDate end,
                                          Long userId,
                                          Long categoryId,
                                          Long supplierId,
                                          Long payerId);

    // --- Por Mês
    @Query(value = """
    SELECT TO_CHAR(e.date, 'YYYY-MM') AS label,
           SUM(e.amount) AS value
    FROM expense e
    WHERE e.date >= COALESCE(:start, e.date)
      AND e.date <= COALESCE(:end, e.date)
      AND (:userId IS NULL OR e.user_id = :userId)
      AND (:categoryId IS NULL OR e.category_id = :categoryId)
      AND (:supplierId IS NULL OR e.supplier_id = :supplierId)
      AND (:payerId IS NULL OR e.payer_id = :payerId)
    GROUP BY TO_CHAR(e.date, 'YYYY-MM')
    ORDER BY TO_CHAR(e.date, 'YYYY-MM')
""", nativeQuery = true)
    List<ChartDataDTO> getTotalByMonth(LocalDate start,
                                       LocalDate end,
                                       Long userId,
                                       Long categoryId,
                                       Long supplierId,
                                       Long payerId);






    // --- Por Fornecedor
    @Query("""
        SELECT s.name AS label, SUM(e.amount) AS value
        FROM Expense e JOIN e.supplier s
        WHERE e.date >= COALESCE(:start, e.date)
          AND e.date <= COALESCE(:end, e.date)
          AND (:userId IS NULL OR e.user.id = :userId)
          AND (:categoryId IS NULL OR e.category.id = :categoryId)
          AND (:payerId IS NULL OR e.payer.id = :payerId)
        GROUP BY s.name
        ORDER BY SUM(e.amount) DESC
    """)
    List<ChartDataDTO> getTotalBySupplier(LocalDate start,
                                          LocalDate end,
                                          Long userId,
                                          Long categoryId,
                                          Long payerId);

    // --- Por Método de Pagamento
    @Query("""
        SELECT e.paymentMethod AS label, SUM(e.amount) AS value
        FROM Expense e
        WHERE e.date >= COALESCE(:start, e.date)
          AND e.date <= COALESCE(:end, e.date)
          AND (:userId IS NULL OR e.user.id = :userId)
          AND (:categoryId IS NULL OR e.category.id = :categoryId)
          AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
          AND (:payerId IS NULL OR e.payer.id = :payerId)
        GROUP BY e.paymentMethod
        ORDER BY SUM(e.amount) DESC
    """)
    List<ChartDataDTO> getTotalByPaymentMethod(LocalDate start,
                                               LocalDate end,
                                               Long userId,
                                               Long categoryId,
                                               Long supplierId,
                                               Long payerId);

    // --- Por Pagador
    @Query("""
        SELECT p.name AS label, SUM(e.amount) AS value
        FROM Expense e JOIN e.payer p
        WHERE e.date >= COALESCE(:start, e.date)
          AND e.date <= COALESCE(:end, e.date)
          AND (:userId IS NULL OR e.user.id = :userId)
          AND (:categoryId IS NULL OR e.category.id = :categoryId)
          AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
        GROUP BY p.name
        ORDER BY SUM(e.amount) DESC
    """)
    List<ChartDataDTO> getTotalByPayer(LocalDate start,
                                       LocalDate end,
                                       Long userId,
                                       Long categoryId,
                                       Long supplierId);

    @Query("""
    SELECT SUM(e.amount) 
    FROM Expense e
    WHERE e.date >= COALESCE(:start, e.date)
      AND e.date <= COALESCE(:end, e.date)
""")
    BigDecimal getTotalExpenses(@Param("start") LocalDate start,
                                @Param("end") LocalDate end);

    @Query("""
    SELECT COUNT(DISTINCT e.payer.id) 
    FROM Expense e
    WHERE e.date >= COALESCE(:start, e.date)
      AND e.date <= COALESCE(:end, e.date)
""")
    Long getTotalPayers(@Param("start") LocalDate start,
                        @Param("end") LocalDate end);

    @Query("""
    SELECT COUNT(DISTINCT e.supplier.id) 
    FROM Expense e
    WHERE e.date >= COALESCE(:start, e.date)
      AND e.date <= COALESCE(:end, e.date)
""")
    Long getTotalSuppliers(@Param("start") LocalDate start,
                           @Param("end") LocalDate end);

    @Query("""
    SELECT COUNT(DISTINCT e.category.id) 
    FROM Expense e
    WHERE e.date >= COALESCE(:start, e.date)
      AND e.date <= COALESCE(:end, e.date)
""")
    Long getTotalCategories(@Param("start") LocalDate start,
                            @Param("end") LocalDate end);


}
