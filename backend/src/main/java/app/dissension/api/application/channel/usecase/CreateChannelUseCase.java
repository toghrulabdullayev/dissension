package app.dissension.api.application.channel.usecase;

import app.dissension.api.application.channel.dto.ChannelResponse;
import app.dissension.api.application.channel.dto.CreateChannelRequest;

import java.util.UUID;

public interface CreateChannelUseCase {
    ChannelResponse createChannel(UUID actorId, UUID serverId, CreateChannelRequest request);
}
