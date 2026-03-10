package app.dissension.api.infrastructure.persistence.auth;

import app.dissension.api.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(UUID userId);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.userId = :userId AND t.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
    void deleteExpiredBefore(@Param("now") Instant now);
}
