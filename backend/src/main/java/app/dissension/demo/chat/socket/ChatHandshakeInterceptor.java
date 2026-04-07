package app.dissension.demo.chat.socket;

import app.dissension.demo.auth.security.JwtService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatHandshakeInterceptor.class);

  private final JwtService jwtService;

  public ChatHandshakeInterceptor(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {
    String rawToken = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
    String token = normalizeToken(rawToken);

    if (token == null || token.isBlank()) {
      LOGGER.warn("WebSocket handshake rejected: missing token uri={}", request.getURI());
      return false;
    }

    try {
      String username = jwtService.extractUsername(token);
      if (!jwtService.isTokenValid(token, username)) {
        LOGGER.warn("WebSocket handshake rejected: invalid token uri={}", request.getURI());
        return false;
      }

      attributes.put("username", username);
      LOGGER.info("WebSocket handshake accepted for user={}", username);
      return true;
    } catch (Exception exception) {
      LOGGER.warn("WebSocket handshake rejected: token parse/validation error uri={}", request.getURI(), exception);
      return false;
    }
  }

  private String normalizeToken(String token) {
    if (token == null) {
      return null;
    }

    if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
      return token.substring(7).trim();
    }

    return token;
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {
    // Nothing to do after handshake.
  }
}
