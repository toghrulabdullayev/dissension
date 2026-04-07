package app.dissension.demo.chat.socket.session;

import org.springframework.stereotype.Component;

@Component
public class ChatWebSocketSessionRegistry extends AbstractSessionRegistry {

  @Override
  protected void onUserBecameOnline(String username) {
    // Lifecycle hook retained for extension points.
  }

  @Override
  protected void onUserBecameOffline(String username) {
    // Lifecycle hook retained for extension points.
  }
}
