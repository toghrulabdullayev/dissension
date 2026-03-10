package app.dissension.api.application.conversation.usecase;

import app.dissension.api.application.conversation.dto.ConversationResponse;

import java.util.List;
import java.util.UUID;

public interface GetUserConversationsUseCase {
    List<ConversationResponse> getUserConversations(UUID userId);
}
