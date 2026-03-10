package app.dissension.api.domain.message.repository;

import app.dissension.api.domain.message.entity.MessageReaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageReactionRepository {
    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(UUID messageId, UUID userId, String emoji);
    List<MessageReaction> findAllByMessageId(UUID messageId);
    MessageReaction save(MessageReaction reaction);
    void delete(MessageReaction reaction);
}
