package app.dissension.api.application.call.usecase;

import app.dissension.api.application.call.dto.CallResponse;

import java.util.Optional;
import java.util.UUID;

public interface GetActiveCallUseCase {
    Optional<CallResponse> getActiveCall(UUID channelId);
}
