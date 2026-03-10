package app.dissension.api.domain.friend.repository;

import app.dissension.api.domain.friend.entity.Friend;
import app.dissension.api.domain.friend.valueobject.FriendStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRepository {
    Optional<Friend> findById(UUID id);
    Optional<Friend> findByUserIdAndFriendId(UUID userId, UUID friendId);
    List<Friend> findAllByUserIdAndStatus(UUID userId, FriendStatus status);

    /**
     * Returns all ACCEPTED friend records for the given user (rows where userId OR friendId matches).
     */
    List<Friend> findAllAcceptedFriends(UUID userId);

    Friend save(Friend friend);
    void delete(Friend friend);
}
