package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.ServiceContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceContractRepository extends JpaRepository<ServiceContract, Long>,
        JpaSpecificationExecutor<ServiceContract> {

    Page<ServiceContract> findByNameIgnoreCase(String name, Pageable pageable);

    Page<ServiceContract> findByDescriptionIgnoreCase(String description, Pageable pageable);

    Optional<ServiceContract> findByNameIgnoreCaseAndDescriptionIgnoreCase(String name, String description);

    Page<ServiceContract> findBySupplierId(Long supplierId, Pageable pageable);

    Page<ServiceContract> findByCategoryId(Long categoryId, Pageable pageable);

}
