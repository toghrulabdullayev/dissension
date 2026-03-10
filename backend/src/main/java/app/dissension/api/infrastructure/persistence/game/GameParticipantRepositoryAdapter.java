package app.dissension.api.infrastructure.persistence.game;

import app.dissension.api.domain.game.entity.GameParticipant;
import app.dissension.api.domain.game.repository.GameParticipantRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GameParticipantRepositoryAdapter implements GameParticipantRepository {

    private final JpaGameParticipantRepository jpa;

    public GameParticipantRepositoryAdapter(JpaGameParticipantRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<GameParticipant> findBySessionIdAndUserId(UUID sessionId, UUID userId) { return jpa.findBySessionIdAndUserId(sessionId, userId); }
    @Override public List<GameParticipant> findAllBySessionIdOrderByScoreDesc(UUID sessionId)        { return jpa.findAllBySessionIdOrderByScoreDesc(sessionId); }
    @Override public boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId)                 { return jpa.existsBySessionIdAndUserId(sessionId, userId); }
    @Override public GameParticipant save(GameParticipant participant)                              { return jpa.save(participant); }
}
