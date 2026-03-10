package app.dissension.api.domain.call.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "calls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "channel_id", nullable = false, updatable = false)
    private UUID channelId;

    @Column(name = "started_at", updatable = false, nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @OneToMany(mappedBy = "call", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CallParticipant> participants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        startedAt = Instant.now();
    }

    public static Call create(UUID channelId) {
        Call call = new Call();
        call.channelId = channelId;
        return call;
    }

    public void end() {
        if (!isActive()) {
            throw new IllegalStateException("Call has already ended");
        }
        this.endedAt = Instant.now();
    }

    public boolean isActive() {
        return endedAt == null;
    }
}
