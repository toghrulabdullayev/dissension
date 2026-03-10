package app.dissension.api.application.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateGroupConversationRequest(
        @NotBlank @Size(max = 100) String name,
        @NotEmpty List<UUID> participantIds
) {}
