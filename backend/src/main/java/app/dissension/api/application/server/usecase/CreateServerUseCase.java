package app.dissension.api.application.server.usecase;

import app.dissension.api.application.server.dto.CreateServerRequest;
import app.dissension.api.application.server.dto.ServerResponse;

import java.util.UUID;

public interface CreateServerUseCase {
    ServerResponse createServer(UUID ownerId, CreateServerRequest request);
}
