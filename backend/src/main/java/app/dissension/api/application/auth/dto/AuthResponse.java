package app.dissension.api.application.auth.dto;

import app.dissension.api.application.user.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        /** Raw opaque token — caller must set this as an httpOnly cookie. Never store in JS. */
        String rawRefreshToken,
        UserResponse user
) {}
