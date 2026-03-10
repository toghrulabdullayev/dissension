package app.dissension.api.infrastructure.persistence.conversation;

import app.dissension.api.domain.conversation.entity.Conversation;
import app.dissension.api.domain.conversation.repository.ConversationRepository;
import app.dissension.api.domain.conversation.valueobject.ConversationType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final JpaConversationRepository jpa;
    private final JpaConversationParticipantRepository participantJpa;

    public ConversationRepositoryAdapter(JpaConversationRepository jpa,
                                         JpaConversationParticipantRepository participantJpa) {
        this.jpa = jpa;
        this.participantJpa = participantJpa;
    }

    @Override
    public Optional<Conversation> findById(UUID id) { return jpa.findById(id); }

    @Override
    public Optional<Conversation> findDirectConversationBetween(UUID userIdA, UUID userIdB) {
        return jpa.findDirectConversationBetween(ConversationType.DIRECT, userIdA, userIdB);
    }

    @Override
    public List<Conversation> findAllByParticipantUserId(UUID userId) {
        return jpa.findAllByParticipantUserId(userId);
    }

    @Override
    public boolean isParticipant(UUID conversationId, UUID userId) {
        return participantJpa.existsByConversation_IdAndUserId(conversationId, userId);
    }

    @Override
    public Conversation save(Conversation conversation) { return jpa.save(conversation); }
}
