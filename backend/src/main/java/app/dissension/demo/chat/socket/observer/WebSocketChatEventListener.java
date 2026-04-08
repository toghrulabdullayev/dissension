package app.dissension.demo.chat.socket.observer;

import app.dissension.demo.chat.socket.event.AbstractChatEvent;
import app.dissension.demo.chat.socket.event.ChatEventListener;
import app.dissension.demo.chat.socket.event.TargetedChatEvent;
import app.dissension.demo.chat.socket.session.ChatWebSocketSessionRegistry;
import java.util.HashSet;
import app.dissension.demo.server.repository.ServerMembershipRepository;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class WebSocketChatEventListener implements ChatEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketChatEventListener.class);

  private final ObjectMapper objectMapper;
  private final ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;
  private final ServerMembershipRepository serverMembershipRepository;

  public WebSocketChatEventListener(
      ObjectMapper objectMapper,
      ChatWebSocketSessionRegistry chatWebSocketSessionRegistry,
      ServerMembershipRepository serverMembershipRepository) {
    this.objectMapper = objectMapper;
    this.chatWebSocketSessionRegistry = chatWebSocketSessionRegistry;
    this.serverMembershipRepository = serverMembershipRepository;
  }

  @Override
  public void onEvent(AbstractChatEvent event) {
    Set<String> usernames = new HashSet<>();

    if (!(event instanceof TargetedChatEvent targetedEvent) || targetedEvent.includeServerMembers()) {
      usernames.addAll(
          serverMembershipRepository.findUsernamesByServerIdOrderByMembershipIdAsc(event.serverId())
              .stream()
              .collect(Collectors.toSet()));
    }

    if (event instanceof TargetedChatEvent targetedEvent) {
      usernames.addAll(targetedEvent.targetUsernames());
    }

    if (usernames.isEmpty()) {
      return;
    }

    try {
      String payload = objectMapper.writeValueAsString(Map.of(
          "type", event.type(),
          "payload", event.payload()));
      chatWebSocketSessionRegistry.sendToUsers(usernames, payload);
    } catch (Exception exception) {
      LOGGER.warn("Failed to broadcast chat event type={} serverId={}", event.type(), event.serverId(), exception);
    }
  }
}
