package app.dissension.api.infrastructure.websocket;

import app.dissension.api.application.auth.port.JwtPort;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * STOMP channel interceptor that validates a JWT on every CONNECT frame.
 * Clients must send: {@code Authorization: Bearer <accessToken>} as a native STOMP header.
 * On success, sets a {@link StompPrincipal} so downstream handlers can identify the user.
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtPort jwtPort;

    public JwtChannelInterceptor(JwtPort jwtPort) {
        this.jwtPort = jwtPort;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Missing or malformed Authorization header in STOMP CONNECT");
        }

        String token = authHeader.substring(7);
        if (!jwtPort.validateAccessToken(token)) {
            throw new BadCredentialsException("Invalid or expired JWT in STOMP CONNECT");
        }

        UUID userId = jwtPort.extractUserId(token);
        accessor.setUser(new StompPrincipal(userId));

        return message;
    }
}
