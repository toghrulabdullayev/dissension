package app.dissension.api.application.auth.usecase;

import app.dissension.api.application.auth.dto.AuthResponse;
import app.dissension.api.application.auth.dto.LoginRequest;

public interface LoginLocalUserUseCase {
    AuthResponse login(LoginRequest request);
}
