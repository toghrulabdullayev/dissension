package app.dissension.api.application.server.usecase;

import java.util.UUID;

public interface DeleteServerUseCase {
    void deleteServer(UUID actorId, UUID serverId);
}
