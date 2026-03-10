package app.dissension.api.domain.message.entity;

import app.dissension.api.domain.message.valueobject.MessageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** Exactly one of channelId / conversationId is non-null. */
    @Column(name = "channel_id")
    private UUID channelId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    /** Nullable — SET NULL when the author's account is deleted. */
    @Column(name = "author_id")
    private UUID authorId;

    /** Null for VOICE messages; cleared on soft-delete of TEXT messages. */
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageReaction> reactions = new ArrayList<>();

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoiceMessage voiceMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    public static Message createChannelTextMessage(UUID channelId, UUID authorId, String content) {
        Message message = new Message();
        message.channelId = channelId;
        message.authorId = authorId;
        message.content = content;
        message.type = MessageType.TEXT;
        return message;
    }

    public static Message createConversationTextMessage(UUID conversationId, UUID authorId, String content) {
        Message message = new Message();
        message.conversationId = conversationId;
        message.authorId = authorId;
        message.content = content;
        message.type = MessageType.TEXT;
        return message;
    }

    public static Message createVoiceMessage(UUID channelId, UUID authorId) {
        Message message = new Message();
        message.channelId = channelId;
        message.authorId = authorId;
        message.type = MessageType.VOICE;
        return message;
    }

    public static Message createSystemMessage(UUID channelId, String content) {
        Message message = new Message();
        message.channelId = channelId;
        message.content = content;
        message.type = MessageType.SYSTEM;
        return message;
    }

    // ── Business methods ─────────────────────────────────────────────────────

    public void edit(String newContent) {
        if (isDeleted()) {
            throw new IllegalStateException("Cannot edit a deleted message");
        }
        if (type != MessageType.TEXT) {
            throw new IllegalStateException("Only TEXT messages can be edited");
        }
        this.content = newContent;
        this.editedAt = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.content = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isEdited() {
        return editedAt != null;
    }
}
