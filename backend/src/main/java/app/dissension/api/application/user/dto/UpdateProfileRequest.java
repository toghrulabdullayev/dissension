package app.dissension.api.application.user.dto;

public record UpdateProfileRequest(
        String username,
        String avatarUrl
) {}
