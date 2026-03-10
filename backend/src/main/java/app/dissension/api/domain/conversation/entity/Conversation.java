package app.dissension.api.domain.conversation.entity;

import app.dissension.api.domain.conversation.valueobject.ConversationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    /** Non-null for GROUP conversations only. */
    @Column(length = 100)
    private String name;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationParticipant> participants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public static Conversation createDirect() {
        Conversation conv = new Conversation();
        conv.type = ConversationType.DIRECT;
        return conv;
    }

    public static Conversation createGroup(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group conversation must have a name");
        }
        Conversation conv = new Conversation();
        conv.type = ConversationType.GROUP;
        conv.name = name.strip();
        return conv;
    }

    public void renameGroup(String name) {
        if (type != ConversationType.GROUP) {
            throw new IllegalStateException("Only GROUP conversations can be renamed");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name must not be blank");
        }
        this.name = name.strip();
    }

    public boolean isDirect() {
        return type == ConversationType.DIRECT;
    }
}
