package app.dissension.api.application.channel.dto;

import jakarta.validation.constraints.Size;

public record UpdateChannelRequest(
        @Size(min = 1, max = 100) String name,
        Integer position
) {}
