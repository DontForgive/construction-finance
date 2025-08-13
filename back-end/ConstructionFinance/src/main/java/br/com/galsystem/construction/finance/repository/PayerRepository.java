package br.com.galsystem.construction.finance.repository;
import br.com.galsystem.construction.finance.models.Payer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PayerRepository  extends JpaRepository<Payer, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
