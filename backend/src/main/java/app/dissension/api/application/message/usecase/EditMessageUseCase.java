package app.dissension.api.application.message.usecase;

import app.dissension.api.application.message.dto.EditMessageRequest;
import app.dissension.api.application.message.dto.MessageResponse;

import java.util.UUID;

public interface EditMessageUseCase {
    MessageResponse editMessage(UUID actorId, UUID messageId, EditMessageRequest request);
}
