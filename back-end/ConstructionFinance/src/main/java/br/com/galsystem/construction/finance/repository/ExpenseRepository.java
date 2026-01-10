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
                  AND (:serviceContractId IS NULL OR e.serviceContract.id = :serviceContractId)
            """)
    Page<Expense> findByFilters(
            @Param("description") String description,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("categoryId") Long categoryId,
            @Param("paymentMethod") String paymentMethod,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("serviceContractId") Long serviceContractId,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Expense e
            WHERE (:supplierId IS NULL OR e.supplier.id = :supplierId)
              AND (:payerId IS NULL OR e.payer.id = :payerId) 
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND ((:paymentMethod IS NULL OR :paymentMethod = '') OR e.paymentMethod = :paymentMethod)
              AND (e.date >= COALESCE(:startDate, e.date))
              AND (e.date <= COALESCE(:endDate, e.date))
              AND (:serviceContractId IS NULL OR e.serviceContract.id = :serviceContractId)
            """)
    List<Expense> findAll(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("categoryId") Long categoryId,
            @Param("serviceContractId") Long serviceContractId,
            @Param("paymentMethod") String paymentMethod
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
                  AND (:serviceContractId IS NULL OR e.serviceContract.id = :serviceContractId)
                GROUP BY c.name
                ORDER BY SUM(e.amount) DESC
            """)
    List<ChartDataDTO> getTotalByCategory(LocalDate start,
                                          LocalDate end,
                                          Long userId,
                                          Long categoryId,
                                          Long supplierId,
                                          Long payerId,
                                          Long serviceContractId);

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
                  AND (:serviceContractId IS NULL OR e.service_contract_id = :serviceContractId)
                GROUP BY TO_CHAR(e.date, 'YYYY-MM')
                ORDER BY TO_CHAR(e.date, 'YYYY-MM')
            """, nativeQuery = true)
    List<ChartDataDTO> getTotalByMonth(LocalDate start,
                                       LocalDate end,
                                       Long userId,
                                       Long categoryId,
                                       Long supplierId,
                                       Long payerId,
                                       Long serviceContractId);


    // --- Por Fornecedor
    @Query("""
                SELECT s.name AS label, SUM(e.amount) AS value
                FROM Expense e JOIN e.supplier s
                WHERE e.date >= COALESCE(:start, e.date)
                  AND e.date <= COALESCE(:end, e.date)
                  AND (:userId IS NULL OR e.user.id = :userId)
                  AND (:categoryId IS NULL OR e.category.id = :categoryId)
                  AND (:payerId IS NULL OR e.payer.id = :payerId)
                  AND (:serviceContractId IS NULL OR e.serviceContract.id = :serviceContractId)
                GROUP BY s.name
                ORDER BY SUM(e.amount) DESC
            """)
    List<ChartDataDTO> getTotalBySupplier(LocalDate start,
                                          LocalDate end,
                                          Long userId,
                                          Long categoryId,
                                          Long payerId,
                                          Long serviceContractId);

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
                  AND (:serviceContractId IS NULL OR e.serviceContract.id = :serviceContractId)
                GROUP BY e.paymentMethod
                ORDER BY SUM(e.amount) DESC
            """)
    List<ChartDataDTO> getTotalByPaymentMethod(LocalDate start,
                                               LocalDate end,
                                               Long userId,
                                               Long categoryId,
                                               Long supplierId,
                                               Long payerId,
                                               Long serviceContractId);


    // --- Por Pagador
    @Query("""
                SELECT p.name AS label, SUM(e.amount) AS value
                FROM Expense e JOIN e.payer p
                WHERE e.date >= COALESCE(:start, e.date)
                  AND e.date <= COALESCE(:end, e.date)
                  AND (:userId IS NULL OR e.user.id = :userId)
                  AND (:categoryId IS NULL OR e.category.id = :categoryId)
                  AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
                  AND (:serviceContractId IS NULL OR e.serviceContract.id = :serviceContractId)
                GROUP BY p.name
                ORDER BY SUM(e.amount) DESC
            """)
    List<ChartDataDTO> getTotalByPayer(LocalDate start,
                                       LocalDate end,
                                       Long userId,
                                       Long categoryId,
                                       Long supplierId,
                                       Long serviceContractId);

    @Query(value = """
            SELECT COALESCE(SUM(e.amount), 0)
            FROM expense e
            WHERE e.date >= COALESCE(:start, e.date)
              AND e.date <= COALESCE(:end, e.date)
              AND e.category_id = COALESCE(:categoryId, e.category_id)
              AND e.supplier_id = COALESCE(:supplierId, e.supplier_id)
              AND e.payer_id = COALESCE(:payerId, e.payer_id)
              AND (:serviceContractId IS NULL OR e.service_contract_id = :serviceContractId)
            """, nativeQuery = true)
    BigDecimal getTotalExpenses(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("categoryId") Long categoryId,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("serviceContractId") Long serviceContractId
    );


    @Query("""
            SELECT COUNT(DISTINCT e.payer.id)
            FROM Expense e
            WHERE (:start IS NULL OR e.date >= :start)
              AND (:end IS NULL OR e.date <= :end)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
              AND (:payerId IS NULL OR e.payer.id = :payerId)
              AND (:serviceContractId IS NULL OR e.serviceContract = :serviceContractId)
            """)
    Long getTotalPayers(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("categoryId") Long categoryId,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("serviceContractId") Long serviceContractId
    );


    @Query("""
            SELECT COUNT(DISTINCT e.supplier.id)
            FROM Expense e
            WHERE (:start IS NULL OR e.date >= :start)
              AND (:end IS NULL OR e.date <= :end)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
              AND (:payerId IS NULL OR e.payer.id = :payerId)
              AND (:serviceContractId IS NULL OR e.serviceContract = :serviceContractId)
            """)
    Long getTotalSuppliers(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("categoryId") Long categoryId,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("serviceContractId") Long serviceContractId
    );


    @Query("""
            SELECT COUNT(DISTINCT e.category.id)
            FROM Expense e
            WHERE (:start IS NULL OR e.date >= :start)
              AND (:end IS NULL OR e.date <= :end)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:supplierId IS NULL OR e.supplier.id = :supplierId)
              AND (:payerId IS NULL OR e.payer.id = :payerId)
              AND (:serviceContractId IS NULL OR e.serviceContract = :serviceContractId)
            """)
    Long getTotalCategories(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("categoryId") Long categoryId,
            @Param("supplierId") Long supplierId,
            @Param("payerId") Long payerId,
            @Param("serviceContractId") Long serviceContractId
    );


}
