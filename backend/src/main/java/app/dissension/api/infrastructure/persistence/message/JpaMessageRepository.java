package app.dissension.api.infrastructure.persistence.message;

import app.dissension.api.domain.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaMessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.channelId = :channelId ORDER BY m.createdAt DESC")
    List<Message> findByChannelId(@Param("channelId") UUID channelId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC")
    List<Message> findByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);
}
