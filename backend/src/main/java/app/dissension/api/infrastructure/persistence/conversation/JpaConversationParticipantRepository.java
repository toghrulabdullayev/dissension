package app.dissension.api.infrastructure.persistence.conversation;

import app.dissension.api.domain.conversation.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {
    boolean existsByConversation_IdAndUserId(UUID conversationId, UUID userId);
}
