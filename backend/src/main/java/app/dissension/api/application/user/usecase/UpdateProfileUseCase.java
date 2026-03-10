package app.dissension.api.application.user.usecase;

import app.dissension.api.application.user.dto.UpdateProfileRequest;
import app.dissension.api.application.user.dto.UserResponse;

import java.util.UUID;

public interface UpdateProfileUseCase {
    UserResponse updateProfile(UUID actorId, UpdateProfileRequest request);
}
