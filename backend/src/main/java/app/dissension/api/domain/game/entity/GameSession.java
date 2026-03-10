package app.dissension.api.domain.game.entity;

import app.dissension.api.domain.game.valueobject.GameSessionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "channel_id", nullable = false, updatable = false)
    private UUID channelId;

    @Column(name = "game_type", nullable = false, length = 100)
    private String gameType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSessionStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameParticipant> participants = new ArrayList<>();

    public static GameSession create(UUID channelId, String gameType) {
        if (gameType == null || gameType.isBlank()) {
            throw new IllegalArgumentException("Game type must not be blank");
        }
        GameSession session = new GameSession();
        session.channelId = channelId;
        session.gameType = gameType.strip();
        session.status = GameSessionStatus.WAITING;
        return session;
    }

    public void start() {
        if (status != GameSessionStatus.WAITING) {
            throw new IllegalStateException("Game session can only be started from WAITING state");
        }
        this.status = GameSessionStatus.ACTIVE;
        this.startedAt = Instant.now();
    }

    public void finish() {
        if (status != GameSessionStatus.ACTIVE) {
            throw new IllegalStateException("Game session can only be finished from ACTIVE state");
        }
        this.status = GameSessionStatus.FINISHED;
        this.endedAt = Instant.now();
    }

    public boolean isActive() {
        return status == GameSessionStatus.ACTIVE;
    }
}
