package app.dissension.api.domain.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "servers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServerMember> members = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public static Server create(String name, UUID ownerId) {
        Server server = new Server();
        server.name = name.strip();
        server.ownerId = ownerId;
        return server;
    }

    public void updateName(String name) {
        this.name = name.strip();
    }

    public void updateIcon(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void transferOwnership(UUID newOwnerId) {
        this.ownerId = newOwnerId;
    }
}
