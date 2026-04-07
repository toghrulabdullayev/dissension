package app.dissension.demo.chat.socket.event;

import java.util.List;
import java.util.UUID;

public record PresenceServerUpdatePayload(
    UUID serverId,
    long onlineMembers,
    List<String> onlineUsernames
) {
}
