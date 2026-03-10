package app.dissension.api.application.friend.service;

import app.dissension.api.application.exception.ConflictException;
import app.dissension.api.application.exception.ForbiddenException;
import app.dissension.api.application.exception.ResourceNotFoundException;
import app.dissension.api.application.friend.dto.FriendResponse;
import app.dissension.api.application.friend.usecase.*;
import app.dissension.api.application.user.dto.UserResponse;
import app.dissension.api.domain.friend.entity.Friend;
import app.dissension.api.domain.friend.repository.FriendRepository;
import app.dissension.api.domain.friend.service.FriendDomainService;
import app.dissension.api.domain.friend.valueobject.FriendStatus;
import app.dissension.api.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FriendService implements SendFriendRequestUseCase, AcceptFriendRequestUseCase,
        DeclineFriendRequestUseCase, RemoveFriendUseCase, BlockUserUseCase, GetFriendsUseCase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final FriendDomainService friendDomainService;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.friendDomainService = new FriendDomainService(friendRepository);
    }

    @Override
    public FriendResponse sendFriendRequest(UUID requesterId, UUID targetId) {
        if (userRepository.findById(targetId).isEmpty()) {
            throw new ResourceNotFoundException("User", targetId);
        }
        try {
            Friend friend = friendDomainService.sendRequest(requesterId, targetId);
            return toResponse(friendRepository.save(friend), requesterId);
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new app.dissension.api.application.exception.ValidationException(e.getMessage());
        }
    }

    @Override
    public FriendResponse acceptFriendRequest(UUID acceptorId, UUID requesterId) {
        Friend request = friendRepository.findByUserIdAndFriendId(requesterId, acceptorId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        try {
            friendDomainService.acceptRequest(request, acceptorId);
        } catch (SecurityException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
        return toResponse(friendRepository.save(request), acceptorId);
    }

    @Override
    public void declineFriendRequest(UUID declinerId, UUID requesterId) {
        Friend request = friendRepository.findByUserIdAndFriendId(requesterId, declinerId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        friendRepository.delete(request);
    }

    @Override
    public void removeFriend(UUID userId, UUID friendId) {
        friendDomainService.removeFriend(userId, friendId);
    }

    @Override
    public void blockUser(UUID blockerId, UUID targetId) {
        // Remove any existing friendship in both directions first
        friendDomainService.removeFriend(blockerId, targetId);
        // Create a new BLOCKED record
        Friend blocked = Friend.createRequest(blockerId, targetId);
        blocked.block();
        friendRepository.save(blocked);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(UUID userId, FriendStatus status) {
        List<Friend> friends = (status == FriendStatus.ACCEPTED)
                ? friendRepository.findAllAcceptedFriends(userId)
                : friendRepository.findAllByUserIdAndStatus(userId, status);

        return friends.stream()
                .map(f -> toResponse(f, userId))
                .toList();
    }

    private FriendResponse toResponse(Friend friend, UUID actorId) {
        UUID friendUserId = friend.getUserId().equals(actorId) ? friend.getFriendId() : friend.getUserId();
        UserResponse friendUser = userRepository.findById(friendUserId)
                .map(UserResponse::from)
                .orElse(null);
        return new FriendResponse(friend.getId(), friendUser, friend.getStatus().name(), friend.getCreatedAt());
    }
}
