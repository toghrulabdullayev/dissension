package app.dissension.demo.server.dto;

import java.util.UUID;

public record DiscoverServerResponse(
    UUID id,
    String name,
    String description,
    String owner,
    long members,
    long onlineMembers,
    boolean joined
) {
}
