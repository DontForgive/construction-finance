package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.Payer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PayerRepository extends JpaRepository<Payer, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Payer> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
                SELECT p FROM Payer p
                WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Payer> findByFilters(String name, Pageable pageable);
}
