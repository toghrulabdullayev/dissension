package app.dissension.api.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_reactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, updatable = false)
    private Message message;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 32)
    private String emoji;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public static MessageReaction create(Message message, UUID userId, String emoji) {
        if (emoji == null || emoji.isBlank()) {
            throw new IllegalArgumentException("Emoji must not be blank");
        }
        MessageReaction reaction = new MessageReaction();
        reaction.message = message;
        reaction.userId = userId;
        reaction.emoji = emoji.strip();
        return reaction;
    }
}
