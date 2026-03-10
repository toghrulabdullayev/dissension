package app.dissension.api.domain.server.entity;

import app.dissension.api.domain.server.valueobject.ServerRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "server_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, updatable = false)
    private Server server;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerRole role;

    @Column(name = "joined_at", updatable = false, nullable = false)
    private Instant joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = Instant.now();
    }

    public static ServerMember create(Server server, UUID userId, ServerRole role) {
        ServerMember member = new ServerMember();
        member.server = server;
        member.userId = userId;
        member.role = role;
        return member;
    }

    public void promoteToMod() {
        if (this.role == ServerRole.MEMBER) {
            this.role = ServerRole.MOD;
        }
    }

    public void demoteToMember() {
        if (this.role == ServerRole.MOD) {
            this.role = ServerRole.MEMBER;
        }
    }
}
