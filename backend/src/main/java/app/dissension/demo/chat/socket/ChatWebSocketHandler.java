package app.dissension.demo.chat.socket;

import app.dissension.demo.chat.dto.ChatMessageResponse;
import app.dissension.demo.chat.service.ChatMessageService;
import app.dissension.demo.chat.service.PresenceService;
import app.dissension.demo.chat.socket.event.ChatEventPublisher;
import app.dissension.demo.chat.socket.event.ChannelMessageCreatedEvent;
import app.dissension.demo.chat.socket.event.PresenceServerUpdatedEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

  private static final String SEND_MESSAGE_TYPE = "send_message";

  private final ObjectMapper objectMapper;
  private final ChatMessageService chatMessageService;
  private final PresenceService presenceService;
  private final ChatEventPublisher chatEventPublisher;

  public ChatWebSocketHandler(
      ObjectMapper objectMapper,
      ChatMessageService chatMessageService,
      PresenceService presenceService,
      ChatEventPublisher chatEventPublisher) {
    this.objectMapper = objectMapper;
    this.chatMessageService = chatMessageService;
    this.presenceService = presenceService;
    this.chatEventPublisher = chatEventPublisher;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String username = getUsername(session);
    if (username == null) {
      LOGGER.warn("WebSocket connection established without username; closing session={}.", session.getId());
      closeQuietly(session, CloseStatus.POLICY_VIOLATION);
      return;
    }

    presenceService.registerSession(username, session);
    LOGGER.info("WebSocket connected user={} session={}", username, session.getId());
    publishPresenceUpdatesForUserServers(username);
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) {
    String username = getUsername(session);
    if (username == null) {
      LOGGER.warn("WebSocket message rejected: missing username session={}", session.getId());
      sendError(session, "Unauthorized websocket session");
      return;
    }

    try {
      SocketCommand command = objectMapper.readValue(message.getPayload(), SocketCommand.class);
      if (!SEND_MESSAGE_TYPE.equals(command.type())) {
        sendError(session, "Unsupported socket command");
        return;
      }

      if (command.serverId() == null || command.channelId() == null) {
        LOGGER.warn("WebSocket message rejected: missing serverId/channelId user={} session={}", username, session.getId());
        sendError(session, "serverId and channelId are required");
        return;
      }

      ChatMessageResponse created = chatMessageService.createMessage(
          command.serverId(),
          command.channelId(),
          username,
          command.content());

      chatEventPublisher.publish(new ChannelMessageCreatedEvent(created));
      LOGGER.info(
          "WebSocket message created id={} serverId={} channelId={} author={}",
          created.id(),
          created.serverId(),
          created.channelId(),
          created.authorUsername());
    } catch (ResponseStatusException exception) {
      LOGGER.warn("WebSocket message rejected user={} reason={}", username, exception.getReason());
      sendError(session, exception.getReason() == null ? "Socket request failed" : exception.getReason());
    } catch (Exception exception) {
      LOGGER.warn("WebSocket message failed user={} session={}", username, session.getId(), exception);
      sendError(session, "Socket request failed");
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    String username = getUsername(session);
    if (username == null) {
      LOGGER.info("WebSocket closed session={} status={} (no username)", session.getId(), status);
      return;
    }

    presenceService.unregisterSession(username, session);
    LOGGER.info("WebSocket disconnected user={} session={} status={}", username, session.getId(), status);
    publishPresenceUpdatesForUserServers(username);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    LOGGER.warn("WebSocket transport error session={}", session.getId(), exception);
    closeQuietly(session, CloseStatus.SERVER_ERROR);
  }

  private String getUsername(WebSocketSession session) {
    Object value = session.getAttributes().get("username");
    if (!(value instanceof String username) || username.isBlank()) {
      return null;
    }

    return username;
  }

  private void publishPresenceUpdatesForUserServers(String username) {
    List<UUID> serverIds = presenceService.getServerIdsForUser(username);
    for (UUID serverId : serverIds) {
      chatEventPublisher.publish(new PresenceServerUpdatedEvent(presenceService.buildServerUpdate(serverId)));
    }
  }

  private void sendError(WebSocketSession session, String message) {
    if (!session.isOpen()) {
      return;
    }

    try {
      String payload = objectMapper.writeValueAsString(Map.of(
          "type", "chat_error",
          "payload", Map.of("message", message, "status", HttpStatus.BAD_REQUEST.value())));
      synchronized (session) {
        session.sendMessage(new TextMessage(payload));
      }
    } catch (Exception ignored) {
      closeQuietly(session, CloseStatus.SERVER_ERROR);
    }
  }

  private void closeQuietly(WebSocketSession session, CloseStatus status) {
    try {
      session.close(status);
    } catch (Exception ignored) {
      // Ignore close failures.
    }
  }

  private record SocketCommand(
      String type,
      UUID serverId,
      UUID channelId,
      String content
  ) {
  }
}
