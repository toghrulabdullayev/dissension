package app.dissension.demo.chat.socket.event;

import java.util.UUID;

public class ServerMembersUpdatedEvent extends AbstractChatEvent {

  private final UUID serverId;

  public ServerMembersUpdatedEvent(UUID serverId) {
    this.serverId = serverId;
  }

  @Override
  public String type() {
    return "server_members_updated";
  }

  @Override
  public UUID serverId() {
    return serverId;
  }

  @Override
  public Object payload() {
    return new ServerMembersUpdatedPayload(serverId);
  }
}
