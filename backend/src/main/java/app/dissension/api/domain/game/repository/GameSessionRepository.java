package app.dissension.api.domain.game.repository;

import app.dissension.api.domain.game.entity.GameSession;

import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository {
    Optional<GameSession> findById(UUID id);
    Optional<GameSession> findActiveSessionByChannelId(UUID channelId);
    GameSession save(GameSession session);
}
