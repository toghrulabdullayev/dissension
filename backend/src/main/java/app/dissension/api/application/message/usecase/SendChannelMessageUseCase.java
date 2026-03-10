package app.dissension.api.application.message.usecase;

import app.dissension.api.application.message.dto.MessageResponse;
import app.dissension.api.application.message.dto.SendMessageRequest;

import java.util.UUID;

public interface SendChannelMessageUseCase {
    MessageResponse sendChannelMessage(UUID senderId, UUID channelId, SendMessageRequest request);
}
