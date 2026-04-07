package app.dissension.demo.chat.socket.session;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public abstract class AbstractSessionRegistry {

  private final ConcurrentHashMap<String, Set<WebSocketSession>> sessionsByUsername = new ConcurrentHashMap<>();

  public final boolean register(String username, WebSocketSession session) {
    String normalizedUsername = normalizeUsername(username);
    Set<WebSocketSession> sessions = sessionsByUsername.computeIfAbsent(normalizedUsername, (key) -> ConcurrentHashMap.newKeySet());
    sessions.add(session);

    boolean isFirstSession = sessions.size() == 1;
    if (isFirstSession) {
      onUserBecameOnline(normalizedUsername);
    }

    return isFirstSession;
  }

  public final boolean unregister(String username, WebSocketSession session) {
    String normalizedUsername = normalizeUsername(username);
    Set<WebSocketSession> sessions = sessionsByUsername.get(normalizedUsername);
    if (sessions == null) {
      return false;
    }

    sessions.remove(session);

    if (!sessions.isEmpty()) {
      return false;
    }

    sessionsByUsername.remove(normalizedUsername);
    onUserBecameOffline(normalizedUsername);
    return true;
  }

  public final boolean isUserOnline(String username) {
    Set<WebSocketSession> sessions = sessionsByUsername.get(normalizeUsername(username));
    return sessions != null && !sessions.isEmpty();
  }

  public final void sendToUsers(Set<String> usernames, String payload) {
    for (String username : usernames) {
      sendToUser(username, payload);
    }
  }

  public final void sendToUser(String username, String payload) {
    Set<WebSocketSession> sessions = sessionsByUsername.get(normalizeUsername(username));
    if (sessions == null || sessions.isEmpty()) {
      return;
    }

    TextMessage textMessage = new TextMessage(payload);

    for (WebSocketSession session : sessions) {
      if (!session.isOpen()) {
        continue;
      }

      try {
        synchronized (session) {
          session.sendMessage(textMessage);
        }
      } catch (IOException ignored) {
        // Ignore single-session send failures and continue with other sessions.
      }
    }
  }

  private String normalizeUsername(String username) {
    return username.toLowerCase(Locale.ROOT);
  }

  protected abstract void onUserBecameOnline(String username);

  protected abstract void onUserBecameOffline(String username);
}
