package app.dissension.demo.chat.socket.event;

import java.util.UUID;

public record ServerMembersUpdatedPayload(UUID serverId) {
}
