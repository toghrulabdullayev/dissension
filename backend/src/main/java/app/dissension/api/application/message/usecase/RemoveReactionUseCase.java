package app.dissension.api.application.message.usecase;

import java.util.UUID;

public interface RemoveReactionUseCase {
    void removeReaction(UUID userId, UUID messageId, String emoji);
}
