package app.dissension.api.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record AddReactionRequest(@NotBlank String emoji) {}
