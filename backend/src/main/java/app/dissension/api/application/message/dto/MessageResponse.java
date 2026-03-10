package app.dissension.api.application.message.dto;

import app.dissension.api.domain.message.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID channelId,
        UUID conversationId,
        UUID authorId,
        String content,
        String type,
        Instant createdAt,
        Instant editedAt,
        boolean deleted,
        List<ReactionResponse> reactions
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChannelId(),
                message.getConversationId(),
                message.getAuthorId(),
                message.getContent(),
                message.getType().name(),
                message.getCreatedAt(),
                message.getEditedAt(),
                message.isDeleted(),
                message.getReactions().stream().map(ReactionResponse::from).toList()
        );
    }
}
