package app.dissension.api.domain.friend.entity;

import app.dissension.api.domain.friend.valueobject.FriendStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friends")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** The user who initiated the relationship (requester or blocker). */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /** The other party in the relationship. */
    @Column(name = "friend_id", nullable = false, updatable = false)
    private UUID friendId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public static Friend createRequest(UUID requesterId, UUID targetId) {
        if (requesterId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }
        Friend friend = new Friend();
        friend.userId = requesterId;
        friend.friendId = targetId;
        friend.status = FriendStatus.PENDING;
        return friend;
    }

    public void accept() {
        if (status != FriendStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be accepted");
        }
        this.status = FriendStatus.ACCEPTED;
    }

    public void block() {
        this.status = FriendStatus.BLOCKED;
    }

    public boolean isPending() {
        return status == FriendStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == FriendStatus.ACCEPTED;
    }
}
