package app.dissension.api.infrastructure.websocket;

import app.dissension.api.application.message.dto.MessageResponse;
import app.dissension.api.application.message.dto.ReactionResponse;
import app.dissension.api.application.realtime.event.*;
import app.dissension.api.application.realtime.port.RealtimeEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Broadcasts application-layer events to STOMP topic destinations.
 *
 * Topic conventions:
 *   /topic/channels/{channelId}          — channel chat events
 *   /topic/conversations/{conversationId} — DM chat events
 *   /topic/presence                       — global presence updates
 */
@Component
public class StompEventPublisher implements RealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public StompEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ---- Channel message events ----

    @Override
    public void publishChannelMessageCreated(UUID channelId, MessageResponse message) {
        send(channelTopic(channelId), new WsEvent(WsEventType.MESSAGE_CREATED, message));
    }

    @Override
    public void publishChannelMessageUpdated(UUID channelId, MessageResponse message) {
        send(channelTopic(channelId), new WsEvent(WsEventType.MESSAGE_UPDATED, message));
    }

    @Override
    public void publishChannelMessageDeleted(UUID channelId, UUID messageId) {
        send(channelTopic(channelId), new WsEvent(WsEventType.MESSAGE_DELETED, new MessageDeletedPayload(messageId)));
    }

    // ---- Conversation message events ----

    @Override
    public void publishConversationMessageCreated(UUID conversationId, MessageResponse message) {
        send(conversationTopic(conversationId), new WsEvent(WsEventType.MESSAGE_CREATED, message));
    }

    @Override
    public void publishConversationMessageUpdated(UUID conversationId, MessageResponse message) {
        send(conversationTopic(conversationId), new WsEvent(WsEventType.MESSAGE_UPDATED, message));
    }

    @Override
    public void publishConversationMessageDeleted(UUID conversationId, UUID messageId) {
        send(conversationTopic(conversationId), new WsEvent(WsEventType.MESSAGE_DELETED, new MessageDeletedPayload(messageId)));
    }

    // ---- Reaction events ----

    @Override
    public void publishReactionAdded(UUID channelId, UUID conversationId, ReactionResponse reaction) {
        String topic = channelId != null ? channelTopic(channelId) : conversationTopic(conversationId);
        send(topic, new WsEvent(WsEventType.REACTION_ADDED, reaction));
    }

    @Override
    public void publishReactionRemoved(UUID channelId, UUID conversationId, UUID messageId, String emoji, UUID userId) {
        String topic = channelId != null ? channelTopic(channelId) : conversationTopic(conversationId);
        send(topic, new WsEvent(WsEventType.REACTION_REMOVED, new ReactionRemovedPayload(messageId, emoji, userId)));
    }

    // ---- Internal helpers ----

    public void publishPresenceUpdate(UUID userId, String status) {
        send("/topic/presence", new WsEvent(WsEventType.PRESENCE_UPDATE, new PresenceEvent(userId, status)));
    }

    public void publishTypingStart(String topic, UUID userId, UUID channelId, UUID conversationId) {
        send(topic, new WsEvent(WsEventType.TYPING_START, new TypingEvent(userId, channelId, conversationId)));
    }

    public void publishTypingStop(String topic, UUID userId, UUID channelId, UUID conversationId) {
        send(topic, new WsEvent(WsEventType.TYPING_STOP, new TypingEvent(userId, channelId, conversationId)));
    }

    private void send(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }

    private static String channelTopic(UUID channelId) {
        return "/topic/channels/" + channelId;
    }

    private static String conversationTopic(UUID conversationId) {
        return "/topic/conversations/" + conversationId;
    }
}
