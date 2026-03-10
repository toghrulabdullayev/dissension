package app.dissension.api.infrastructure.persistence.message;

import app.dissension.api.domain.message.entity.MessageReaction;
import app.dissension.api.domain.message.repository.MessageReactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MessageReactionRepositoryAdapter implements MessageReactionRepository {

    private final JpaMessageReactionRepository jpa;

    public MessageReactionRepositoryAdapter(JpaMessageReactionRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(UUID messageId, UUID userId, String emoji) {
        return jpa.findByMessageIdAndUserIdAndEmoji(messageId, userId, emoji);
    }
    @Override public List<MessageReaction> findAllByMessageId(UUID messageId) { return jpa.findAllByMessageId(messageId); }
    @Override public MessageReaction save(MessageReaction reaction)           { return jpa.save(reaction); }
    @Override public void delete(MessageReaction reaction)                    { jpa.delete(reaction); }
}
