package app.dissension.demo.chat.service;

import app.dissension.demo.chat.socket.event.PresenceServerUpdatePayload;
import app.dissension.demo.chat.socket.session.ChatWebSocketSessionRegistry;
import app.dissension.demo.server.repository.ServerMembershipRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
public class PresenceService {

  private final ServerMembershipRepository serverMembershipRepository;
  private final ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;

  public PresenceService(
      ServerMembershipRepository serverMembershipRepository,
      ChatWebSocketSessionRegistry chatWebSocketSessionRegistry) {
    this.serverMembershipRepository = serverMembershipRepository;
    this.chatWebSocketSessionRegistry = chatWebSocketSessionRegistry;
  }

  public boolean registerSession(String username, WebSocketSession session) {
    return chatWebSocketSessionRegistry.register(username, session);
  }

  public boolean unregisterSession(String username, WebSocketSession session) {
    return chatWebSocketSessionRegistry.unregister(username, session);
  }

  public boolean isUserOnline(String username) {
    return chatWebSocketSessionRegistry.isUserOnline(username);
  }

  public long countOnlineMembers(UUID serverId) {
    return serverMembershipRepository.findUsernamesByServerIdOrderByMembershipIdAsc(serverId)
      .stream()
        .filter(this::isUserOnline)
        .count();
  }

  public List<String> getOnlineUsernames(UUID serverId) {
    return serverMembershipRepository.findUsernamesByServerIdOrderByMembershipIdAsc(serverId)
        .stream()
        .filter(this::isUserOnline)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();
  }

  public List<UUID> getServerIdsForUser(String username) {
    return serverMembershipRepository.findServerIdsByUsernameOrderByMembershipIdAsc(username)
      .stream()
        .distinct()
        .sorted(Comparator.comparing(UUID::toString))
        .toList();
  }

  public PresenceServerUpdatePayload buildServerUpdate(UUID serverId) {
    List<String> onlineUsernames = getOnlineUsernames(serverId);

    return new PresenceServerUpdatePayload(
        serverId,
        onlineUsernames.size(),
        onlineUsernames.stream().collect(Collectors.toList()));
  }
}
