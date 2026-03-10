package app.dissension.api.infrastructure.persistence.conversation;

import app.dissension.api.domain.conversation.entity.Conversation;
import app.dissension.api.domain.conversation.valueobject.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.type = :type
              AND EXISTS (SELECT p FROM ConversationParticipant p WHERE p.conversation = c AND p.userId = :userIdA)
              AND EXISTS (SELECT p FROM ConversationParticipant p WHERE p.conversation = c AND p.userId = :userIdB)
            """)
    Optional<Conversation> findDirectConversationBetween(
            @Param("type") ConversationType type,
            @Param("userIdA") UUID userIdA,
            @Param("userIdB") UUID userIdB);

    @Query("SELECT DISTINCT c FROM Conversation c JOIN c.participants p WHERE p.userId = :userId")
    List<Conversation> findAllByParticipantUserId(@Param("userId") UUID userId);
}
