package app.dissension.api.application.auth.usecase;

public interface LogoutUseCase {
    /** Revokes the refresh token, invalidating the session. */
    void logout(String rawRefreshToken);
}
