package app.dissension.api.application.game.dto;

import app.dissension.api.domain.game.entity.GameParticipant;

import java.util.UUID;

public record GameParticipantResponse(
        UUID id,
        UUID userId,
        int score
) {
    public static GameParticipantResponse from(GameParticipant participant) {
        return new GameParticipantResponse(
                participant.getId(),
                participant.getUserId(),
                participant.getScore()
        );
    }
}
