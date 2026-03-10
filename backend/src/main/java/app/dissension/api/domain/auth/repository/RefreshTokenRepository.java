package app.dissension.api.domain.auth.repository;

import app.dissension.api.domain.auth.entity.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(UUID userId);

    RefreshToken save(RefreshToken token);

    /** Marks all active tokens for a user as revoked (used on logout-all-devices). */
    void revokeAllByUserId(UUID userId);

    /** Deletes tokens that are past their expiry date. Called by a scheduled task. */
    void deleteExpiredTokens();
}
