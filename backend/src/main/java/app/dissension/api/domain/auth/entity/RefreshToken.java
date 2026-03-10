package app.dissension.api.domain.auth.entity;

import app.dissension.api.domain.auth.valueobject.TokenHash;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for a refresh token.
 *
 * Only the SHA-256 hash of the raw token is stored.
 * The raw token is returned once to the client and never persisted.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** References the user that owns this token — cross-aggregate by ID. */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        revoked = false;
    }

    // ------------------------------------------------------------------
    // Static factory
    // ------------------------------------------------------------------

    public static RefreshToken create(UUID userId, TokenHash tokenHash, Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        token.userId = userId;
        token.tokenHash = tokenHash.value();
        token.expiresAt = expiresAt;
        token.revoked = false;
        return token;
    }

    // ------------------------------------------------------------------
    // Business methods
    // ------------------------------------------------------------------

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
