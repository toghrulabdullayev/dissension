package app.dissension.api.application.server.usecase;

import app.dissension.api.application.server.dto.ServerResponse;

import java.util.List;
import java.util.UUID;

public interface GetUserServersUseCase {
    List<ServerResponse> getUserServers(UUID userId);
}
