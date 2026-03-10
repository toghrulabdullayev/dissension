package app.dissension.api.application.realtime.event;

import java.util.UUID;

/**
 * Payload for PRESENCE_UPDATE events broadcast to all subscribers of /topic/presence.
 *
 * @param userId the user whose presence changed
 * @param status the new presence status (ONLINE, AWAY, OFFLINE)
 */
public record PresenceEvent(UUID userId, String status) {}
