package app.dissension.demo.chat.socket.event;

public interface ChatEventListener {

  void onEvent(AbstractChatEvent event);
}
