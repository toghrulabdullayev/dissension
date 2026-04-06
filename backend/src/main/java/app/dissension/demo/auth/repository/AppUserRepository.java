package app.dissension.demo.auth.repository;

import app.dissension.demo.auth.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// by extending JpaRepository, Spring automatically provides common db operations for AppUser
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  // this method name automatically builds this query: SELECT * FROM app_users WHERE username = ?
  Optional<AppUser> findByUsername(String username);

  // the same happens with this method
  boolean existsByUsername(String username);
}
