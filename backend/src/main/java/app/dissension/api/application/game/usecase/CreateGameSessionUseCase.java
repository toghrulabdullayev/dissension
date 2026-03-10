package app.dissension.api.application.game.usecase;

import app.dissension.api.application.game.dto.CreateGameSessionRequest;
import app.dissension.api.application.game.dto.GameSessionResponse;

import java.util.UUID;

public interface CreateGameSessionUseCase {
    GameSessionResponse createGameSession(UUID hostId, UUID channelId, CreateGameSessionRequest request);
}
