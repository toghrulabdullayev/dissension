package app.dissension.demo.server.dto;

import app.dissension.demo.server.model.ServerRole;
import jakarta.validation.constraints.NotNull;

public record UpdateServerMemberRoleRequest(
    @NotNull(message = "Role is required")
    ServerRole role
) {
}
