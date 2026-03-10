package app.dissension.api.application.channel.dto;

import app.dissension.api.domain.channel.entity.Channel;

import java.time.Instant;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        UUID serverId,
        String name,
        String type,
        int position,
        Instant createdAt
) {
    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getServerId(),
                channel.getName(),
                channel.getType().name(),
                channel.getPosition(),
                channel.getCreatedAt()
        );
    }
}
