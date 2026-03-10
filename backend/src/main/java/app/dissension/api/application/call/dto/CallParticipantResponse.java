package app.dissension.api.application.call.dto;

import app.dissension.api.domain.call.entity.CallParticipant;

import java.time.Instant;
import java.util.UUID;

public record CallParticipantResponse(
        UUID id,
        UUID userId,
        Instant joinedAt,
        Instant leftAt,
        boolean active
) {
    public static CallParticipantResponse from(CallParticipant participant) {
        return new CallParticipantResponse(
                participant.getId(),
                participant.getUserId(),
                participant.getJoinedAt(),
                participant.getLeftAt(),
                participant.isActive()
        );
    }
}
