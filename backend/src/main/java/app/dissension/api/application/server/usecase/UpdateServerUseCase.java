package app.dissension.api.application.server.usecase;

import app.dissension.api.application.server.dto.ServerResponse;
import app.dissension.api.application.server.dto.UpdateServerRequest;

import java.util.UUID;

public interface UpdateServerUseCase {
    ServerResponse updateServer(UUID actorId, UUID serverId, UpdateServerRequest request);
}
