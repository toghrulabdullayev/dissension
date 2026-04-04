package app.dissension.demo.server.dto;

import app.dissension.demo.server.model.ServerRole;

public record ServerResponse(
    Long id,
    String name,
    ServerRole role
) {
}
