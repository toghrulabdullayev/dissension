package app.dissension.api.domain.call.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "call_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false, updatable = false)
    private Call call;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "joined_at", updatable = false, nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = Instant.now();
    }

    public static CallParticipant create(Call call, UUID userId) {
        CallParticipant participant = new CallParticipant();
        participant.call = call;
        participant.userId = userId;
        return participant;
    }

    public void leave() {
        if (!isActive()) {
            throw new IllegalStateException("Participant has already left the call");
        }
        this.leftAt = Instant.now();
    }

    public boolean isActive() {
        return leftAt == null;
    }
}
