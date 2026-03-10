package app.dissension.api.application.server.dto;

import app.dissension.api.domain.server.entity.Server;

import java.time.Instant;
import java.util.UUID;

public record ServerResponse(
        UUID id,
        String name,
        UUID ownerId,
        String iconUrl,
        Instant createdAt,
        int memberCount
) {
    public static ServerResponse from(Server server) {
        return new ServerResponse(
                server.getId(),
                server.getName(),
                server.getOwnerId(),
                server.getIconUrl(),
                server.getCreatedAt(),
                server.getMembers().size()
        );
    }
}
