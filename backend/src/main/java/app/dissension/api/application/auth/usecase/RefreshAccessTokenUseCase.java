package app.dissension.api.application.auth.usecase;

import app.dissension.api.application.auth.dto.TokenRefreshResponse;

public interface RefreshAccessTokenUseCase {
    /** Validates the raw refresh token, rotates it, and issues a new access token. */
    TokenRefreshResponse refreshToken(String rawRefreshToken);
}
