package app.dissension.api.infrastructure.persistence.message;

import app.dissension.api.domain.message.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaMessageReactionRepository extends JpaRepository<MessageReaction, UUID> {

    @Query("SELECT r FROM MessageReaction r WHERE r.message.id = :messageId AND r.userId = :userId AND r.emoji = :emoji")
    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(
            @Param("messageId") UUID messageId,
            @Param("userId") UUID userId,
            @Param("emoji") String emoji);

    @Query("SELECT r FROM MessageReaction r WHERE r.message.id = :messageId")
    List<MessageReaction> findAllByMessageId(@Param("messageId") UUID messageId);
}
