package app.dissension.api.application.conversation.dto;

import app.dissension.api.domain.conversation.entity.Conversation;
import app.dissension.api.domain.conversation.entity.ConversationParticipant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String type,
        String name,
        List<UUID> participantIds,
        Instant createdAt
) {
    public static ConversationResponse from(Conversation conversation) {
        List<UUID> participantIds = conversation.getParticipants().stream()
                .map(ConversationParticipant::getUserId)
                .toList();
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType().name(),
                conversation.getName(),
                participantIds,
                conversation.getCreatedAt()
        );
    }
}
