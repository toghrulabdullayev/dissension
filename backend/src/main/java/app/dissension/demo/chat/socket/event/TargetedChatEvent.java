package app.dissension.demo.chat.socket.event;

import java.util.Set;

public interface TargetedChatEvent {

  Set<String> targetUsernames();

  boolean includeServerMembers();
}
