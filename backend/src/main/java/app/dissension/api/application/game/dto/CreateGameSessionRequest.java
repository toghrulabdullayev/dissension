package app.dissension.api.application.game.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGameSessionRequest(
        @NotBlank String gameType
) {}
