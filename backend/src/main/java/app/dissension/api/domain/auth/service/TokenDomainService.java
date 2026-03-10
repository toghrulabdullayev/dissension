package app.dissension.api.domain.auth.service;

import app.dissension.api.domain.auth.entity.RefreshToken;
import app.dissension.api.domain.auth.valueobject.TokenHash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Domain service for refresh token lifecycle.
 *
 * Responsibilities:
 *  - Generate cryptographically random raw tokens
 *  - Hash raw tokens with SHA-256 before storage
 *  - Create RefreshToken aggregates
 *  - Validate an incoming raw token against a stored hash
 *
 * No Spring annotations — instantiated and injected by the application layer.
 */
public class TokenDomainService {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 30;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a URL-safe, base64-encoded random token string.
     * This raw token is returned to the client and must NOT be stored.
     */
    public String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Produces a SHA-256 hex hash of the raw token for safe DB storage.
     */
    public TokenHash hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new TokenHash(HexFormat.of().formatHex(hashBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available on this JVM", e);
        }
    }

    /**
     * Creates a new RefreshToken aggregate ready for persistence.
     * The caller is responsible for persisting the returned entity
     * and sending the raw token to the client.
     */
    public RefreshToken createRefreshToken(UUID userId, String rawToken) {
        TokenHash hash = hashToken(rawToken);
        Instant expiresAt = Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS);
        return RefreshToken.create(userId, hash, expiresAt);
    }

    /**
     * Returns true if the stored token is valid and the raw token matches its hash.
     */
    public boolean validateRefreshToken(RefreshToken storedToken, String rawToken) {
        if (!storedToken.isValid()) {
            return false;
        }
        TokenHash expectedHash = hashToken(rawToken);
        return storedToken.getTokenHash().equals(expectedHash.value());
    }
}
