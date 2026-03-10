package app.dissension.api.infrastructure.persistence.user;

import app.dissension.api.domain.user.entity.User;
import app.dissension.api.domain.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    public UserRepositoryAdapter(JpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<User> findById(UUID id)            { return jpa.findById(id); }
    @Override public Optional<User> findByEmail(String email)    { return jpa.findByEmail(email); }
    @Override public Optional<User> findByUsername(String name)  { return jpa.findByUsername(name); }
    @Override public boolean existsByEmail(String email)         { return jpa.existsByEmail(email); }
    @Override public boolean existsByUsername(String name)       { return jpa.existsByUsername(name); }
    @Override public User save(User user)                        { return jpa.save(user); }
    @Override public void deleteById(UUID id)                    { jpa.deleteById(id); }
}
