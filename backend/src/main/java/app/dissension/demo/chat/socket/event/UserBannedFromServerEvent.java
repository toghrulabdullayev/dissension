package app.dissension.demo.chat.socket.event;

import java.util.Set;
import java.util.UUID;

public class UserBannedFromServerEvent extends AbstractChatEvent implements TargetedChatEvent {

  private final UserBannedFromServerPayload payload;
  private final String bannedUsername;

  public UserBannedFromServerEvent(
      UUID serverId,
      String serverName,
      String bannedByUsername,
      String bannedUsername) {
    this.payload = new UserBannedFromServerPayload(serverId, serverName, bannedByUsername);
    this.bannedUsername = bannedUsername;
  }

  @Override
  public String type() {
    return "user_banned_from_server";
  }

  @Override
  public UUID serverId() {
    return payload.serverId();
  }

  @Override
  public Object payload() {
    return payload;
  }

  @Override
  public Set<String> targetUsernames() {
    return Set.of(bannedUsername);
  }

  @Override
  public boolean includeServerMembers() {
    return false;
  }
}
