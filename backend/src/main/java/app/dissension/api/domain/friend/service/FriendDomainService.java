package app.dissension.api.domain.friend.service;

import app.dissension.api.domain.friend.entity.Friend;
import app.dissension.api.domain.friend.repository.FriendRepository;

import java.util.UUID;

/**
 * Domain service for Friend aggregate operations.
 * No Spring annotations — instantiated by the application layer.
 */
public class FriendDomainService {

    private final FriendRepository friendRepository;

    public FriendDomainService(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    /**
     * Validates and creates a new pending friend request.
     * Checks both directions to prevent duplicates.
     * Caller is responsible for persisting the returned entity.
     */
    public Friend sendRequest(UUID requesterId, UUID targetId) {
        if (requesterId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }
        boolean alreadyExists =
                friendRepository.findByUserIdAndFriendId(requesterId, targetId).isPresent() ||
                friendRepository.findByUserIdAndFriendId(targetId, requesterId).isPresent();
        if (alreadyExists) {
            throw new IllegalStateException("A friend relationship already exists between these users");
        }
        return Friend.createRequest(requesterId, targetId);
    }

    /**
     * Accepts an incoming friend request.
     * Only the request recipient (friendId) may accept.
     */
    public void acceptRequest(Friend request, UUID acceptorId) {
        if (!request.getFriendId().equals(acceptorId)) {
            throw new SecurityException("Only the request recipient can accept it");
        }
        request.accept();
    }

    /**
     * Removes the friendship in both directions.
     */
    public void removeFriend(UUID userId, UUID friendId) {
        friendRepository.findByUserIdAndFriendId(userId, friendId).ifPresent(friendRepository::delete);
        friendRepository.findByUserIdAndFriendId(friendId, userId).ifPresent(friendRepository::delete);
    }
}
