package app.dissension.api.application.call.service;

import app.dissension.api.application.call.dto.CallResponse;
import app.dissension.api.application.call.usecase.*;
import app.dissension.api.application.exception.ConflictException;
import app.dissension.api.application.exception.ResourceNotFoundException;
import app.dissension.api.domain.call.entity.Call;
import app.dissension.api.domain.call.entity.CallParticipant;
import app.dissension.api.domain.call.repository.CallRepository;
import app.dissension.api.domain.call.service.CallDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CallService implements StartCallUseCase, JoinCallUseCase, LeaveCallUseCase, GetActiveCallUseCase {

    private final CallRepository callRepository;
    private final CallDomainService callDomainService;

    public CallService(CallRepository callRepository) {
        this.callRepository = callRepository;
        this.callDomainService = new CallDomainService(callRepository);
    }

    @Override
    public CallResponse startCall(UUID callerId, UUID channelId) {
        try {
            Call call = callDomainService.startCall(channelId);
            call = callRepository.save(call);
            CallParticipant caller = callDomainService.joinCall(call, callerId);
            call.getParticipants().add(caller);
            return CallResponse.from(callRepository.save(call));
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    public CallResponse joinCall(UUID userId, UUID callId) {
        Call call = requireCall(callId);
        try {
            CallParticipant participant = callDomainService.joinCall(call, userId);
            call.getParticipants().add(participant);
            return CallResponse.from(callRepository.save(call));
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    public void leaveCall(UUID userId, UUID callId) {
        Call call = requireCall(callId);
        callDomainService.leaveCall(call, userId);
        callRepository.save(call);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CallResponse> getActiveCall(UUID channelId) {
        return callRepository.findActiveCallByChannelId(channelId)
                .map(CallResponse::from);
    }

    private Call requireCall(UUID callId) {
        return callRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException("Call", callId));
    }
}
