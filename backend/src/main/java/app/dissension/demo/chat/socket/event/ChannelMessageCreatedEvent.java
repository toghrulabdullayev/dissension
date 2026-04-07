package app.dissension.demo.chat.socket.event;

import app.dissension.demo.chat.dto.ChatMessageResponse;
import java.util.UUID;

public class ChannelMessageCreatedEvent extends AbstractChatEvent {

  private final ChatMessageResponse message;

  public ChannelMessageCreatedEvent(ChatMessageResponse message) {
    this.message = message;
  }

  @Override
  public String type() {
    return "chat_message_created";
  }

  @Override
  public UUID serverId() {
    return message.serverId();
  }

  @Override
  public Object payload() {
    return message;
  }

  public ChatMessageResponse message() {
    return message;
  }
}
