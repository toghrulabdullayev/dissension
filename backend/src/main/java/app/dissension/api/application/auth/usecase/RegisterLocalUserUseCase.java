package app.dissension.api.application.auth.usecase;

import app.dissension.api.application.auth.dto.AuthResponse;
import app.dissension.api.application.auth.dto.RegisterRequest;

public interface RegisterLocalUserUseCase {
    AuthResponse register(RegisterRequest request);
}
