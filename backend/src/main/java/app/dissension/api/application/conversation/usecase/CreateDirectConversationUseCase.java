package app.dissension.api.application.conversation.usecase;

import app.dissension.api.application.conversation.dto.ConversationResponse;

import java.util.UUID;

public interface CreateDirectConversationUseCase {
    ConversationResponse createDirectConversation(UUID userId, UUID targetUserId);
}
