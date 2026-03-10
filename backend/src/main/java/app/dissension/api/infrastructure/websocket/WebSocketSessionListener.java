package app.dissension.api.infrastructure.websocket;

import app.dissension.api.application.realtime.port.PresencePort;
import app.dissension.api.application.realtime.port.TypingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

/**
 * Listens to Spring WebSocket session lifecycle events to automatically
 * manage user presence.
 *
 * CONNECT  → mark user ONLINE, broadcast PRESENCE_UPDATE
 * DISCONNECT → clear all typing keys for user, mark OFFLINE, broadcast PRESENCE_UPDATE
 */
@Component
public class WebSocketSessionListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionListener.class);

    private final PresencePort presencePort;
    private final StompEventPublisher publisher;

    public WebSocketSessionListener(PresencePort presencePort, StompEventPublisher publisher) {
        this.presencePort = presencePort;
        this.publisher = publisher;
    }

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        UUID userId = extractUserId(principal);
        presencePort.setOnline(userId);
        publisher.publishPresenceUpdate(userId, "ONLINE");
        log.debug("User {} connected — marked ONLINE", userId);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) return;

        UUID userId = extractUserId(principal);
        presencePort.setOffline(userId);
        publisher.publishPresenceUpdate(userId, "OFFLINE");
        log.debug("User {} disconnected — marked OFFLINE", userId);
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof StompPrincipal sp) {
            return sp.userId();
        }
        return UUID.fromString(principal.getName());
    }
}
