package app.dissension.api.application.channel.usecase;

import app.dissension.api.application.channel.dto.ChannelResponse;

import java.util.List;
import java.util.UUID;

public interface GetChannelsByServerUseCase {
    List<ChannelResponse> getChannelsByServer(UUID serverId, UUID requesterId);
}
