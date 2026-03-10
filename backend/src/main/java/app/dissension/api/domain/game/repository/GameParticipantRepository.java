package app.dissension.api.domain.game.repository;

import app.dissension.api.domain.game.entity.GameParticipant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameParticipantRepository {
    Optional<GameParticipant> findBySessionIdAndUserId(UUID sessionId, UUID userId);
    List<GameParticipant> findAllBySessionIdOrderByScoreDesc(UUID sessionId);
    boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId);
    GameParticipant save(GameParticipant participant);
}
