package app.dissension.api.application.game.dto;

import app.dissension.api.domain.game.entity.GameSession;
import app.dissension.api.domain.game.entity.GameParticipant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameSessionResponse(
        UUID id,
        UUID channelId,
        String gameType,
        String status,
        Instant startedAt,
        Instant endedAt,
        List<GameParticipantResponse> participants
) {
    public static GameSessionResponse from(GameSession session) {
        List<GameParticipantResponse> participants = session.getParticipants().stream()
                .map(GameParticipantResponse::from)
                .toList();
        return new GameSessionResponse(
                session.getId(),
                session.getChannelId(),
                session.getGameType(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getEndedAt(),
                participants
        );
    }
}
