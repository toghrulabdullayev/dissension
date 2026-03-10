package app.dissension.api.application.realtime.event;

import java.util.UUID;

/**
 * Payload for MESSAGE_DELETED events so clients can remove the message from their state.
 */
public record MessageDeletedPayload(UUID messageId) {}
