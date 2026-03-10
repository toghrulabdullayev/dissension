package app.dissension.api.application.message.usecase;

import app.dissension.api.application.message.dto.ReactionResponse;

import java.util.UUID;

public interface AddReactionUseCase {
    ReactionResponse addReaction(UUID userId, UUID messageId, String emoji);
}
