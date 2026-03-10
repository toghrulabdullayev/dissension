package app.dissension.api.application.realtime.event;

import java.util.UUID;

/**
 * Payload for TYPING_START / TYPING_STOP events.
 *
 * @param userId         the user who is (or stopped) typing
 * @param channelId      set when typing in a server channel (null for DMs)
 * @param conversationId set when typing in a DM conversation (null for channels)
 */
public record TypingEvent(UUID userId, UUID channelId, UUID conversationId) {}
