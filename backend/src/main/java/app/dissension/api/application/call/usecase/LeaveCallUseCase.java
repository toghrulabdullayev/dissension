package app.dissension.api.application.call.usecase;

import java.util.UUID;

public interface LeaveCallUseCase {
    void leaveCall(UUID userId, UUID callId);
}
