package app.dissension.demo.chat.repository;

import app.dissension.demo.chat.entity.ChatMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  List<ChatMessage> findTop100ByChannelIdOrderByCreatedAtDesc(UUID channelId);
}
