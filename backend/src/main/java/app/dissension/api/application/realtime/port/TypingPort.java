package app.dissension.api.application.realtime.port;

import java.util.UUID;

/**
 * Port for tracking typing indicators.
 * Implemented by infrastructure using Redis TTL keys.
 */
public interface TypingPort {

    /**
     * Records that a user started typing in a channel.
     * Returns true if this is a new typing event (key was absent), false if it was a refresh.
     */
    boolean startTypingInChannel(UUID userId, UUID channelId);

    /**
     * Records that a user started typing in a conversation.
     * Returns true if this is a new typing event.
     */
    boolean startTypingInConversation(UUID userId, UUID conversationId);

    /**
     * Explicitly stops typing indicator for a channel (called when user sends message or leaves).
     * Returns true if key was present (so the caller should broadcast TYPING_STOP).
     */
    boolean stopTypingInChannel(UUID userId, UUID channelId);

    /** Explicitly stops typing indicator for a conversation. */
    boolean stopTypingInConversation(UUID userId, UUID conversationId);
}
