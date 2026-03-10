package app.dissension.api.application.message.dto;

import app.dissension.api.domain.message.entity.MessageReaction;

import java.time.Instant;
import java.util.UUID;

public record ReactionResponse(
        UUID id,
        UUID messageId,
        UUID userId,
        String emoji,
        Instant createdAt
) {
    public static ReactionResponse from(MessageReaction reaction) {
        return new ReactionResponse(
                reaction.getId(),
                reaction.getMessage().getId(),
                reaction.getUserId(),
                reaction.getEmoji(),
                reaction.getCreatedAt()
        );
    }
}
