package app.dissension.api.application.server.usecase;

import app.dissension.api.application.server.dto.ServerMemberResponse;

import java.util.UUID;

public interface JoinServerUseCase {
    ServerMemberResponse joinServer(UUID userId, UUID serverId);
}
