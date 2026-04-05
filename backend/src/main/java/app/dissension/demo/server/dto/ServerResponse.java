package app.dissension.demo.server.dto;

import app.dissension.demo.server.model.ServerRole;
import java.util.UUID;

public record ServerResponse(
    UUID id,
    String name,
    String description,
    long members,
    ServerRole role
) {
}
