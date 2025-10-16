package br.com.galsystem.construction.finance.repository;

import br.com.galsystem.construction.finance.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("select u.id from User u where u.username = :username")
    Optional<Long> findIdByUsername(String username);

    @Query("""
                SELECT s FROM User s
                WHERE (:username IS NULL OR LOWER(s.username) LIKE LOWER(CONCAT('%', :username, '%')))
                  AND (:email IS NULL OR LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')))
            """)
    Page<User> findByFilters(@Param("username") String username, @Param("email") String email, Pageable pageable);


}
