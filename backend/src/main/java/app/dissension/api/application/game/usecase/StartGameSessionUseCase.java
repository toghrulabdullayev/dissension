package app.dissension.api.application.game.usecase;

import app.dissension.api.application.game.dto.GameSessionResponse;

import java.util.UUID;

public interface StartGameSessionUseCase {
    GameSessionResponse startGameSession(UUID actorId, UUID sessionId);
}
