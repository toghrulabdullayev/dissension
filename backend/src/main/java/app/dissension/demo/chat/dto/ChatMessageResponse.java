package app.dissension.demo.chat.dto;

import java.util.UUID;

public record ChatMessageResponse(
    UUID id,
    UUID serverId,
    UUID channelId,
    String authorUsername,
    String authorImageUrl,
    String content,
    String createdAt
) {
}
