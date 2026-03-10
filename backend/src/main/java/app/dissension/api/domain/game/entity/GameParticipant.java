package app.dissension.api.domain.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "game_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, updatable = false)
    private GameSession session;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private int score;

    public static GameParticipant create(GameSession session, UUID userId) {
        GameParticipant participant = new GameParticipant();
        participant.session = session;
        participant.userId = userId;
        participant.score = 0;
        return participant;
    }

    public void updateScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
        this.score = score;
    }

    public void addPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points to add must not be negative");
        }
        this.score += points;
    }
}
