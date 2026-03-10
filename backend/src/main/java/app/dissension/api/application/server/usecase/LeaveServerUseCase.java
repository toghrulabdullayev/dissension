package app.dissension.api.application.server.usecase;

import java.util.UUID;

public interface LeaveServerUseCase {
    void leaveServer(UUID userId, UUID serverId);
}
