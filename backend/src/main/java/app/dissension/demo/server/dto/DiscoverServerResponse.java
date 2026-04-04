package app.dissension.demo.server.dto;

public record DiscoverServerResponse(
    Long id,
    String name,
    String description,
    String owner,
    long members,
    long onlineMembers,
    boolean joined
) {
}
