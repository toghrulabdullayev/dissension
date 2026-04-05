package app.dissension.demo.server.dto;

import app.dissension.demo.server.model.ServerRole;

public record ServerMemberResponse(
    String username,
    String imageUrl,
    ServerRole role
) {
}
