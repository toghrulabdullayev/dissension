package app.dissension.api.application.friend.usecase;

import java.util.UUID;

public interface BlockUserUseCase {
    void blockUser(UUID blockerId, UUID targetId);
}
