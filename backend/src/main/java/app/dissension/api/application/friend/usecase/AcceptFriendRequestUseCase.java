package app.dissension.api.application.friend.usecase;

import app.dissension.api.application.friend.dto.FriendResponse;

import java.util.UUID;

public interface AcceptFriendRequestUseCase {
    FriendResponse acceptFriendRequest(UUID acceptorId, UUID requesterId);
}
