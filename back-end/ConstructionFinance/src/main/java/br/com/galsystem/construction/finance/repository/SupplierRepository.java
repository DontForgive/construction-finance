package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Supplier> findByNameIgnoreCase(String name);

    @Query("""
                SELECT s FROM Supplier s
                WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
                             AND (:worker IS NULL OR s.worker = :worker)
            """)
    Page<Supplier> findByFilters(@Param("name") String name, @Param("worker") Boolean worker, Pageable pageable);

}
