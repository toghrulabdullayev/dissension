package app.dissension.api.application.channel.usecase;

import app.dissension.api.application.channel.dto.ChannelResponse;
import app.dissension.api.application.channel.dto.UpdateChannelRequest;

import java.util.UUID;

public interface UpdateChannelUseCase {
    ChannelResponse updateChannel(UUID actorId, UUID channelId, UpdateChannelRequest request);
}
