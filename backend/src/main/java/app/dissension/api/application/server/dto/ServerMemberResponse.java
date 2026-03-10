package app.dissension.api.application.server.dto;

import app.dissension.api.domain.server.entity.ServerMember;

import java.time.Instant;
import java.util.UUID;

public record ServerMemberResponse(
        UUID id,
        UUID serverId,
        UUID userId,
        String role,
        Instant joinedAt
) {
    public static ServerMemberResponse from(ServerMember member) {
        return new ServerMemberResponse(
                member.getId(),
                member.getServer().getId(),
                member.getUserId(),
                member.getRole().name(),
                member.getJoinedAt()
        );
    }
}
