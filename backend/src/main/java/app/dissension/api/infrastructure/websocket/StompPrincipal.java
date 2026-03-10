package app.dissension.api.infrastructure.websocket;

import java.security.Principal;
import java.util.UUID;

/**
 * Represents an authenticated WebSocket/STOMP user.
 * Set as the principal on the STOMP session after JWT validation in {@link JwtChannelInterceptor}.
 */
public record StompPrincipal(UUID userId) implements Principal {

    @Override
    public String getName() {
        return userId.toString();
    }
}
