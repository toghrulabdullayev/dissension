package app.dissension.demo.chat.socket.event;

import java.util.UUID;

public record UserBannedFromServerPayload(
    UUID serverId,
    String serverName,
    String bannedByUsername
) {
}
