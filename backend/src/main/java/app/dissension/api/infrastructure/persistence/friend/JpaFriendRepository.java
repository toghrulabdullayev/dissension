package app.dissension.api.infrastructure.persistence.friend;

import app.dissension.api.domain.friend.entity.Friend;
import app.dissension.api.domain.friend.valueobject.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaFriendRepository extends JpaRepository<Friend, UUID> {

    Optional<Friend> findByUserIdAndFriendId(UUID userId, UUID friendId);

    List<Friend> findAllByUserIdAndStatus(UUID userId, FriendStatus status);

    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = :status")
    List<Friend> findAllAcceptedFriends(@Param("userId") UUID userId, @Param("status") FriendStatus status);
}
