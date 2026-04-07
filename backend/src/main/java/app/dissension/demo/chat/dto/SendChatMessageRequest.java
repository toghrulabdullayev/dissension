package app.dissension.demo.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    String content
) {
}
