package app.dissension.api.application.server.usecase;

import app.dissension.api.application.server.dto.ServerResponse;

import java.util.UUID;

public interface GetServerUseCase {
    ServerResponse getServer(UUID serverId, UUID requesterId);
}
