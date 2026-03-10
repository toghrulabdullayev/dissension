package app.dissension.api.domain.auth.valueobject;

/**
 * A SHA-256 hex-encoded hash of a raw refresh token.
 * The raw token is never stored — only this value object persists.
 */
public record TokenHash(String value) {

    public TokenHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Token hash must not be blank");
        }
    }
}
