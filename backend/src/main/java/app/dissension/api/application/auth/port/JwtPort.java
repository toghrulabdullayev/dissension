package app.dissension.api.application.auth.port;

import java.util.UUID;

/**
 * Port interface for JWT operations.
 * Implemented by the infrastructure security layer.
 */
public interface JwtPort {
    String issueAccessToken(UUID userId);
    UUID extractUserId(String token);
    boolean validateAccessToken(String token);
}
