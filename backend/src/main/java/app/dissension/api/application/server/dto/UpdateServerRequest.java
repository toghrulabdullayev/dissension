package app.dissension.api.application.server.dto;

import jakarta.validation.constraints.Size;

public record UpdateServerRequest(
        @Size(min = 2, max = 100) String name,
        String iconUrl
) {}
