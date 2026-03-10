package app.dissension.api.application.message.usecase;

import app.dissension.api.application.message.dto.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface GetConversationMessagesUseCase {
    List<MessageResponse> getConversationMessages(UUID requesterId, UUID conversationId, int limit, int offset);
}
