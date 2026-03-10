package app.dissension.api.application.conversation.usecase;

import app.dissension.api.application.conversation.dto.ConversationResponse;
import app.dissension.api.application.conversation.dto.CreateGroupConversationRequest;

import java.util.UUID;

public interface CreateGroupConversationUseCase {
    ConversationResponse createGroupConversation(UUID creatorId, CreateGroupConversationRequest request);
}
