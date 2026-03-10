package app.dissension.api.application.channel.dto;

import app.dissension.api.domain.channel.valueobject.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChannelRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        @NotNull ChannelType type,
        int position
) {}
