package app.dissension.api.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMemberRoleRequest(@NotBlank String role) {}
