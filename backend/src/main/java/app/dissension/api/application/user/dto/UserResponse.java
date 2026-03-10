package app.dissension.api.application.user.dto;

import app.dissension.api.domain.user.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String avatarUrl,
        String status,
        String authProvider,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getStatus().name(),
                user.getAuthProvider().name(),
                user.getCreatedAt()
        );
    }
}
