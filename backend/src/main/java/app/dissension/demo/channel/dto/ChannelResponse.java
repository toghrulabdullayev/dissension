package app.dissension.demo.channel.dto;

import app.dissension.demo.channel.model.ChannelType;
import java.util.UUID;

public record ChannelResponse(
    UUID id,
    String name,
    ChannelType type,
    int position
) {
}
