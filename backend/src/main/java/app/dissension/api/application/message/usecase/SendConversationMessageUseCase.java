package app.dissension.api.application.message.usecase;

import app.dissension.api.application.message.dto.MessageResponse;
import app.dissension.api.application.message.dto.SendMessageRequest;

import java.util.UUID;

public interface SendConversationMessageUseCase {
    MessageResponse sendConversationMessage(UUID senderId, UUID conversationId, SendMessageRequest request);
}
