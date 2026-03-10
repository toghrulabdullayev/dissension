package app.dissension.api.domain.user.repository;

import app.dissension.api.domain.user.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Pure domain repository interface — no Spring/JPA dependency.
 * The infrastructure layer provides the JPA implementation.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User save(User user);

    void deleteById(UUID id);
}
