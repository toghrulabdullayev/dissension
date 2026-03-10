package app.dissension.api.domain.user.entity;

import app.dissension.api.domain.user.valueobject.AuthProvider;
import app.dissension.api.domain.user.valueobject.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for the User domain.
 *
 * Construction is done via static factory methods to make intent explicit.
 * Mutations are explicit business methods — no raw setters exposed.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    /** Null for GOOGLE auth_provider accounts. */
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = UserStatus.OFFLINE;
        }
    }

    // ------------------------------------------------------------------
    // Static factory methods
    // ------------------------------------------------------------------

    public static User createOAuthUser(String username, String email, String avatarUrl) {
        User user = new User();
        user.username = username.strip();
        user.email = email.strip().toLowerCase();
        user.authProvider = AuthProvider.GOOGLE;
        user.avatarUrl = avatarUrl;
        user.status = UserStatus.OFFLINE;
        return user;
    }

    public static User createLocalUser(String username, String email, String passwordHash) {
        User user = new User();
        user.username = username.strip();
        user.email = email.strip().toLowerCase();
        user.passwordHash = passwordHash;
        user.authProvider = AuthProvider.LOCAL;
        user.status = UserStatus.OFFLINE;
        return user;
    }

    // ------------------------------------------------------------------
    // Business methods
    // ------------------------------------------------------------------

    public void updateStatus(UserStatus newStatus) {
        this.status = newStatus;
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void updateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        this.username = username.strip();
    }

    public boolean isOAuthUser() {
        return authProvider == AuthProvider.GOOGLE;
    }
}
