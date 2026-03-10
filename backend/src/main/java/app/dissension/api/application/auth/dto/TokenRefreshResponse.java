package app.dissension.api.application.auth.dto;

public record TokenRefreshResponse(
        String accessToken,
        /** Rotated refresh token — caller replaces the httpOnly cookie. */
        String rawRefreshToken
) {}
