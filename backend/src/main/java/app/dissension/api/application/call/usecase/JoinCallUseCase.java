package app.dissension.api.application.call.usecase;

import app.dissension.api.application.call.dto.CallResponse;

import java.util.UUID;

public interface JoinCallUseCase {
    CallResponse joinCall(UUID userId, UUID callId);
}
