package app.dissension.api.domain.message.repository;

import app.dissension.api.domain.message.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Optional<Message> findById(UUID id);
    List<Message> findByChannelId(UUID channelId, int limit, int offset);
    List<Message> findByConversationId(UUID conversationId, int limit, int offset);
    Message save(Message message);
    void delete(Message message);
}
