package app.dissension.api.application.message.usecase;

import app.dissension.api.application.message.dto.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface GetChannelMessagesUseCase {
    List<MessageResponse> getChannelMessages(UUID requesterId, UUID channelId, int limit, int offset);
}
