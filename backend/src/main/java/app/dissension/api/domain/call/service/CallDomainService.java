package app.dissension.api.domain.call.service;

import app.dissension.api.domain.call.entity.Call;
import app.dissension.api.domain.call.entity.CallParticipant;
import app.dissension.api.domain.call.repository.CallRepository;

import java.util.UUID;

/**
 * Domain service for Call aggregate lifecycle.
 * No Spring annotations — instantiated by the application layer.
 */
public class CallDomainService {

    private final CallRepository callRepository;

    public CallDomainService(CallRepository callRepository) {
        this.callRepository = callRepository;
    }

    /**
     * Creates a new call on a channel, enforcing that only one active call
     * can exist per channel at a time.
     * Caller is responsible for persisting the returned entity.
     */
    public Call startCall(UUID channelId) {
        callRepository.findActiveCallByChannelId(channelId).ifPresent(existing -> {
            throw new IllegalStateException("A call is already active in this channel");
        });
        return Call.create(channelId);
    }

    /**
     * Adds a participant to an ongoing call.
     * Returns the new participant entity; caller persists it.
     */
    public CallParticipant joinCall(Call call, UUID userId) {
        if (!call.isActive()) {
            throw new IllegalStateException("Cannot join a call that has ended");
        }
        boolean alreadyIn = call.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId) && p.isActive());
        if (alreadyIn) {
            throw new IllegalStateException("User is already an active participant in this call");
        }
        return CallParticipant.create(call, userId);
    }

    /**
     * Removes a participant and automatically ends the call
     * when the last active participant leaves.
     */
    public void leaveCall(Call call, UUID userId) {
        CallParticipant participant = call.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId) && p.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not an active participant in this call"));

        participant.leave();

        boolean anyStillActive = call.getParticipants().stream().anyMatch(CallParticipant::isActive);
        if (!anyStillActive) {
            call.end();
        }
    }
}
