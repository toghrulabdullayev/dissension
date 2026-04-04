package app.dissension.demo.channel.dto;

import app.dissension.demo.channel.model.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChannelRequest(
    @NotBlank(message = "Channel name is required")
    @Size(min = 2, max = 100, message = "Channel name must be between 2 and 100 characters")
    String name,

    @NotNull(message = "Channel type is required")
    ChannelType type
) {
}
