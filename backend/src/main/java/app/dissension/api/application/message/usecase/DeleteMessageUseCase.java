package app.dissension.api.application.message.usecase;

import java.util.UUID;

public interface DeleteMessageUseCase {
    void deleteMessage(UUID actorId, UUID messageId);
}
