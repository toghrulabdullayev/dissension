package app.dissension.api.presentation.websocket;

import app.dissension.api.application.realtime.dto.SetPresenceRequest;
import app.dissension.api.application.realtime.dto.TypingRequest;
import app.dissension.api.application.realtime.port.PresencePort;
import app.dissension.api.application.realtime.port.TypingPort;
import app.dissension.api.infrastructure.websocket.StompEventPublisher;
import app.dissension.api.infrastructure.websocket.StompPrincipal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Handles inbound STOMP messages from connected clients.
 *
 * Client destinations (message to server):
 *   /app/channels/{channelId}/typing        — user is typing in a text channel
 *   /app/conversations/{conversationId}/typing — user is typing in a DM
 *   /app/presence                            — set own presence status
 *
 * Outbound topics (server broadcasts):
 *   /topic/channels/{channelId}             — TYPING_START / TYPING_STOP
 *   /topic/conversations/{conversationId}   — TYPING_START / TYPING_STOP
 *   /topic/presence                         — PRESENCE_UPDATE
 */
@Controller
public class RealtimeController {

    private final TypingPort typingPort;
    private final PresencePort presencePort;
    private final StompEventPublisher publisher;

    public RealtimeController(TypingPort typingPort,
                              PresencePort presencePort,
                              StompEventPublisher publisher) {
        this.typingPort = typingPort;
        this.presencePort = presencePort;
        this.publisher = publisher;
    }

    @MessageMapping("/channels/{channelId}/typing")
    public void channelTyping(@DestinationVariable UUID channelId,
                              @Payload(required = false) TypingRequest ignored,
                              Principal principal) {
        UUID userId = extractUserId(principal);
        boolean isNew = typingPort.startTypingInChannel(userId, channelId);
        if (isNew) {
            publisher.publishTypingStart("/topic/channels/" + channelId, userId, channelId, null);
        }
    }

    @MessageMapping("/conversations/{conversationId}/typing")
    public void conversationTyping(@DestinationVariable UUID conversationId,
                                   @Payload(required = false) TypingRequest ignored,
                                   Principal principal) {
        UUID userId = extractUserId(principal);
        boolean isNew = typingPort.startTypingInConversation(userId, conversationId);
        if (isNew) {
            publisher.publishTypingStart("/topic/conversations/" + conversationId, userId, null, conversationId);
        }
    }

    @MessageMapping("/presence")
    public void setPresence(@Payload SetPresenceRequest request,
                            Principal principal) {
        UUID userId = extractUserId(principal);
        String status = request.status() == null ? "ONLINE" : request.status().toUpperCase();
        switch (status) {
            case "AWAY" -> presencePort.setAway(userId);
            case "OFFLINE" -> presencePort.setOffline(userId);
            default -> presencePort.setOnline(userId);
        }
        publisher.publishPresenceUpdate(userId, presencePort.getStatus(userId));
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof StompPrincipal sp) {
            return sp.userId();
        }
        return UUID.fromString(principal.getName());
    }
}
