package app.dissension.demo.channel.dto;

import app.dissension.demo.channel.model.ChannelType;

public record ChannelResponse(
    Long id,
    String name,
    ChannelType type,
    int position
) {
}
