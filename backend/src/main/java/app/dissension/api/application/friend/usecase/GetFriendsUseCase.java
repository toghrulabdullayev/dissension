package app.dissension.api.application.friend.usecase;

import app.dissension.api.application.friend.dto.FriendResponse;
import app.dissension.api.domain.friend.valueobject.FriendStatus;

import java.util.List;
import java.util.UUID;

public interface GetFriendsUseCase {
    List<FriendResponse> getFriends(UUID userId, FriendStatus status);
}
