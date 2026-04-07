package app.dissension.demo.chat.socket.event;

import java.util.UUID;

public abstract class AbstractChatEvent {

  public abstract String type();

  public abstract UUID serverId();

  public abstract Object payload();
}
