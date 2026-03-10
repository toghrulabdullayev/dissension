package app.dissension.api.application.realtime.dto;

import java.util.UUID;

/**
 * Sent by the client to /app/channels/{channelId}/typing or
 * /app/conversations/{conversationId}/typing to signal typing activity.
 *
 * No fields needed — the sender identity comes from the STOMP principal
 * and the target ID from the destination path variable.
 * This empty record is kept for extensibility.
 */
public record TypingRequest() {}
