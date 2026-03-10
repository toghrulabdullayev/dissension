package app.dissension.api.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "voice_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, updatable = false, unique = true)
    private Message message;

    @Column(name = "audio_url", nullable = false, columnDefinition = "TEXT")
    private String audioUrl;

    /** Duration in seconds. */
    @Column(nullable = false)
    private int duration;

    public static VoiceMessage create(Message message, String audioUrl, int durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0 seconds");
        }
        if (audioUrl == null || audioUrl.isBlank()) {
            throw new IllegalArgumentException("Audio URL must not be blank");
        }
        VoiceMessage vm = new VoiceMessage();
        vm.message = message;
        vm.audioUrl = audioUrl;
        vm.duration = durationSeconds;
        return vm;
    }
}
