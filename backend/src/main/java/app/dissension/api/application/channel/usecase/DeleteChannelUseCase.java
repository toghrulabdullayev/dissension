package app.dissension.api.application.channel.usecase;

import java.util.UUID;

public interface DeleteChannelUseCase {
    void deleteChannel(UUID actorId, UUID channelId);
}
