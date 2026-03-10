package app.dissension.api.infrastructure.persistence.game;

import app.dissension.api.domain.game.entity.GameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaGameParticipantRepository extends JpaRepository<GameParticipant, UUID> {

    @Query("SELECT p FROM GameParticipant p WHERE p.session.id = :sessionId AND p.userId = :userId")
    Optional<GameParticipant> findBySessionIdAndUserId(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId);

    @Query("SELECT p FROM GameParticipant p WHERE p.session.id = :sessionId ORDER BY p.score DESC")
    List<GameParticipant> findAllBySessionIdOrderByScoreDesc(@Param("sessionId") UUID sessionId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM GameParticipant p WHERE p.session.id = :sessionId AND p.userId = :userId")
    boolean existsBySessionIdAndUserId(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId);
}
