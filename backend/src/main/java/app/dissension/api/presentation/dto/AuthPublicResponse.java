package app.dissension.api.presentation.dto;

import app.dissension.api.application.user.dto.UserResponse;

public record AuthPublicResponse(String accessToken, UserResponse user) {}
