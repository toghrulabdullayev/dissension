package app.dissension.api.application.friend.dto;

import app.dissension.api.application.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record FriendResponse(
        UUID id,
        UserResponse friend,
        String status,
        Instant createdAt
) {}
