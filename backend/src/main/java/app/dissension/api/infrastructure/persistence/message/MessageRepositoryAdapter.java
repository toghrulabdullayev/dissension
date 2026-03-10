package app.dissension.api.infrastructure.persistence.message;

import app.dissension.api.domain.message.entity.Message;
import app.dissension.api.domain.message.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MessageRepositoryAdapter implements MessageRepository {

    private final JpaMessageRepository jpa;

    public MessageRepositoryAdapter(JpaMessageRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Message> findById(UUID id) { return jpa.findById(id); }

    @Override
    public List<Message> findByChannelId(UUID channelId, int limit, int offset) {
        int page = limit > 0 ? offset / limit : 0;
        return jpa.findByChannelId(channelId, PageRequest.of(page, limit));
    }

    @Override
    public List<Message> findByConversationId(UUID conversationId, int limit, int offset) {
        int page = limit > 0 ? offset / limit : 0;
        return jpa.findByConversationId(conversationId, PageRequest.of(page, limit));
    }

    @Override public Message save(Message message)   { return jpa.save(message); }
    @Override public void delete(Message message)    { jpa.delete(message); }
}
