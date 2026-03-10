package app.dissension.api.domain.message.service;

import app.dissension.api.domain.message.entity.Message;
import app.dissension.api.domain.message.entity.MessageReaction;
import app.dissension.api.domain.message.repository.MessageReactionRepository;

import java.util.UUID;

/**
 * Domain service for Message aggregate operations.
 * No Spring annotations — instantiated by the application layer.
 */
public class MessageDomainService {

    private final MessageReactionRepository messageReactionRepository;

    public MessageDomainService(MessageReactionRepository messageReactionRepository) {
        this.messageReactionRepository = messageReactionRepository;
    }

    /**
     * Adds a reaction to a message, enforcing the uniqueness rule.
     * Returns the new reaction entity; caller persists it.
     */
    public MessageReaction addReaction(Message message, UUID userId, String emoji) {
        if (message.isDeleted()) {
            throw new IllegalStateException("Cannot react to a deleted message");
        }
        messageReactionRepository.findByMessageIdAndUserIdAndEmoji(message.getId(), userId, emoji)
                .ifPresent(r -> { throw new IllegalStateException("User has already added this reaction"); });

        return MessageReaction.create(message, userId, emoji);
    }

    /**
     * Removes a reaction from a message.
     */
    public void removeReaction(Message message, UUID userId, String emoji) {
        MessageReaction reaction = messageReactionRepository
                .findByMessageIdAndUserIdAndEmoji(message.getId(), userId, emoji)
                .orElseThrow(() -> new IllegalArgumentException("Reaction not found"));
        messageReactionRepository.delete(reaction);
    }

    /**
     * Asserts that the actor is the message author and the message is editable.
     */
    public void assertCanEdit(Message message, UUID actorUserId) {
        if (message.isDeleted()) {
            throw new IllegalStateException("Cannot edit a deleted message");
        }
        if (!message.getAuthorId().equals(actorUserId)) {
            throw new SecurityException("Only the message author can edit this message");
        }
    }

    /**
     * Asserts that the actor may delete the message.
     * Server MOD/OWNER override is a cross-domain concern handled in the application layer.
     */
    public void assertCanDelete(Message message, UUID actorUserId) {
        if (message.isDeleted()) {
            throw new IllegalStateException("Message is already deleted");
        }
        if (!message.getAuthorId().equals(actorUserId)) {
            throw new SecurityException("Only the message author can delete this message");
        }
    }
}
