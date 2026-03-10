package app.dissension.api.application.friend.usecase;

import java.util.UUID;

public interface RemoveFriendUseCase {
    void removeFriend(UUID userId, UUID friendId);
}
