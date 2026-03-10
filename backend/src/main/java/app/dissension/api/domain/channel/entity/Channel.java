package app.dissension.api.domain.channel.entity;

import app.dissension.api.domain.channel.valueobject.ChannelType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "channels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "server_id", nullable = false, updatable = false)
    private UUID serverId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public static Channel create(UUID serverId, String name, ChannelType type, int position) {
        Channel channel = new Channel();
        channel.serverId = serverId;
        channel.name = name.strip();
        channel.type = type;
        channel.position = position;
        return channel;
    }

    public void rename(String name) {
        this.name = name.strip();
    }

    public void reorder(int position) {
        this.position = position;
    }

    public boolean isVoiceBased() {
        return type.isVoiceBased();
    }
}
