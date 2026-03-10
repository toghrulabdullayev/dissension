package app.dissension.api.application.call.dto;

import app.dissension.api.domain.call.entity.Call;
import app.dissension.api.domain.call.entity.CallParticipant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CallResponse(
        UUID id,
        UUID channelId,
        Instant startedAt,
        Instant endedAt,
        boolean active,
        List<CallParticipantResponse> participants
) {
    public static CallResponse from(Call call) {
        List<CallParticipantResponse> participants = call.getParticipants().stream()
                .map(CallParticipantResponse::from)
                .toList();
        return new CallResponse(
                call.getId(),
                call.getChannelId(),
                call.getStartedAt(),
                call.getEndedAt(),
                call.isActive(),
                participants
        );
    }
}
