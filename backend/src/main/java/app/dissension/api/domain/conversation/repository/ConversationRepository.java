package app.dissension.api.domain.conversation.repository;

import app.dissension.api.domain.conversation.entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {
    Optional<Conversation> findById(UUID id);

    /**
     * Finds a DIRECT conversation between exactly two users.
     * Returns empty if no such conversation exists.
     */
    Optional<Conversation> findDirectConversationBetween(UUID userIdA, UUID userIdB);

    List<Conversation> findAllByParticipantUserId(UUID userId);
    boolean isParticipant(UUID conversationId, UUID userId);
    Conversation save(Conversation conversation);
}
