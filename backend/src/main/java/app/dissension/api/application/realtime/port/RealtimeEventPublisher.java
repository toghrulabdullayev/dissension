package app.dissension.api.application.realtime.port;

import app.dissension.api.application.message.dto.MessageResponse;
import app.dissension.api.application.message.dto.ReactionResponse;

import java.util.UUID;

/**
 * Port for broadcasting realtime events to WebSocket subscribers.
 * Implemented by the infrastructure layer using SimpMessagingTemplate.
 */
public interface RealtimeEventPublisher {

    // ---- Channel message events ----
    void publishChannelMessageCreated(UUID channelId, MessageResponse message);
    void publishChannelMessageUpdated(UUID channelId, MessageResponse message);
    void publishChannelMessageDeleted(UUID channelId, UUID messageId);

    // ---- Conversation message events ----
    void publishConversationMessageCreated(UUID conversationId, MessageResponse message);
    void publishConversationMessageUpdated(UUID conversationId, MessageResponse message);
    void publishConversationMessageDeleted(UUID conversationId, UUID messageId);

    // ---- Reaction events ----
    void publishReactionAdded(UUID channelId, UUID conversationId, ReactionResponse reaction);
    void publishReactionRemoved(UUID channelId, UUID conversationId, UUID messageId, String emoji, UUID userId);
}
