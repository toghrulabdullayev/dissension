package app.dissension.api.application.realtime.event;

import java.util.UUID;

/**
 * Payload for REACTION_REMOVED events.
 */
public record ReactionRemovedPayload(UUID messageId, String emoji, UUID userId) {}
