package app.dissension.demo.chat.socket.event;

import java.util.UUID;

public class PresenceServerUpdatedEvent extends AbstractChatEvent {

  private final PresenceServerUpdatePayload payload;

  public PresenceServerUpdatedEvent(PresenceServerUpdatePayload payload) {
    this.payload = payload;
  }

  @Override
  public String type() {
    return "presence_server_updated";
  }

  @Override
  public UUID serverId() {
    return payload.serverId();
  }

  @Override
  public Object payload() {
    return payload;
  }

  public PresenceServerUpdatePayload presence() {
    return payload;
  }
}
