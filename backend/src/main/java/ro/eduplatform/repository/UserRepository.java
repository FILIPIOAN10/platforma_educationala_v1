package ro.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.eduplatform.domain.Role;
import ro.eduplatform.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
