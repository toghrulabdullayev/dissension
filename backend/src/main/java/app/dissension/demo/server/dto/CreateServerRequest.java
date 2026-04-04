package app.dissension.demo.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateServerRequest(
    @NotBlank(message = "Server name is required")
    @Size(min = 2, max = 100, message = "Server name must be between 2 and 100 characters")
    String name,
    @Size(max = 128, message = "Server description must be at most 128 characters")
    String description
) {
}
