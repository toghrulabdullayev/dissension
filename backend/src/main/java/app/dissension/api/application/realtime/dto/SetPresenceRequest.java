package app.dissension.api.application.realtime.dto;

/**
 * Sent by the client to /app/presence to explicitly set their status.
 *
 * @param status ONLINE | AWAY (OFFLINE is set automatically on WebSocket disconnect)
 */
public record SetPresenceRequest(String status) {}
