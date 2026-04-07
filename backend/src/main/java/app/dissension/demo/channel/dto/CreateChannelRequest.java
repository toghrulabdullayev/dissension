package app.dissension.demo.channel.dto;

import app.dissension.demo.channel.model.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChannelRequest(
  // no whitespaces only, no empty strings, no null values
  @NotBlank(message = "Channel name is required")
  @Size(min = 2, max = 100, message = "Channel name must be between 2 and 100 characters")
  String name,

  // everything but null value
  @NotNull(message = "Channel type is required")
  ChannelType type
) {
}
