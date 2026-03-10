package app.dissension.api.application.auth.usecase;

import app.dissension.api.application.auth.dto.AuthResponse;

public interface ProcessOAuthLoginUseCase {
    /** Called by the OAuth2 success handler with data from the Google profile. */
    AuthResponse processOAuthLogin(String email, String displayName, String avatarUrl);
}
