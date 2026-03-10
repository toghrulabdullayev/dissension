package app.dissension.api.application.call.usecase;

import app.dissension.api.application.call.dto.CallResponse;

import java.util.UUID;

public interface StartCallUseCase {
    CallResponse startCall(UUID callerId, UUID channelId);
}
