package app.dissension.api.application.user.usecase;

import app.dissension.api.application.user.dto.UserResponse;

import java.util.UUID;

public interface GetUserUseCase {
    UserResponse getUser(UUID userId);
}
