package app.dissension.api.application.friend.usecase;

import java.util.UUID;

public interface DeclineFriendRequestUseCase {
    void declineFriendRequest(UUID declinerId, UUID requesterId);
}
