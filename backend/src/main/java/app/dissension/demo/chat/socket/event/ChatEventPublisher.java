package app.dissension.demo.chat.socket.event;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher {

  private final List<ChatEventListener> listeners;

  public ChatEventPublisher(List<ChatEventListener> listeners) {
    this.listeners = listeners;
  }

  public void publish(AbstractChatEvent event) {
    for (ChatEventListener listener : listeners) {
      listener.onEvent(event);
    }
  }
}
