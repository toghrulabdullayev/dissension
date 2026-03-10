package app.dissension.api.infrastructure.persistence.game;

import app.dissension.api.domain.game.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaGameSessionRepository extends JpaRepository<GameSession, UUID> {

    @Query("SELECT s FROM GameSession s WHERE s.channelId = :channelId AND s.status = app.dissension.api.domain.game.valueobject.GameSessionStatus.ACTIVE")
    Optional<GameSession> findActiveSessionByChannelId(@Param("channelId") UUID channelId);
}
