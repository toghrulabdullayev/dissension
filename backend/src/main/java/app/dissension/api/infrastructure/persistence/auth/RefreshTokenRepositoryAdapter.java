package app.dissension.api.infrastructure.persistence.auth;

import app.dissension.api.domain.auth.entity.RefreshToken;
import app.dissension.api.domain.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpa;

    public RefreshTokenRepositoryAdapter(JpaRefreshTokenRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<RefreshToken> findByTokenHash(String tokenHash) { return jpa.findByTokenHash(tokenHash); }
    @Override public List<RefreshToken> findAllByUserId(UUID userId)           { return jpa.findAllByUserId(userId); }
    @Override public RefreshToken save(RefreshToken token)                     { return jpa.save(token); }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) { jpa.revokeAllByUserId(userId); }

    @Override
    @Transactional
    public void deleteExpiredTokens() { jpa.deleteExpiredBefore(Instant.now()); }
}
