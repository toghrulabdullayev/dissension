package app.dissension.api.infrastructure.persistence.game;

import app.dissension.api.domain.game.entity.GameSession;
import app.dissension.api.domain.game.repository.GameSessionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class GameSessionRepositoryAdapter implements GameSessionRepository {

    private final JpaGameSessionRepository jpa;

    public GameSessionRepositoryAdapter(JpaGameSessionRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<GameSession> findById(UUID id)                        { return jpa.findById(id); }
    @Override public Optional<GameSession> findActiveSessionByChannelId(UUID channelId) { return jpa.findActiveSessionByChannelId(channelId); }
    @Override public GameSession save(GameSession session)                          { return jpa.save(session); }
}
