package app.dissension.api.presentation.controller;

import app.dissension.api.application.call.dto.CallResponse;
import app.dissension.api.application.call.usecase.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/calls")
public class CallController {

    private final StartCallUseCase startCallUseCase;
    private final JoinCallUseCase joinCallUseCase;
    private final LeaveCallUseCase leaveCallUseCase;
    private final GetActiveCallUseCase getActiveCallUseCase;

    public CallController(StartCallUseCase startCallUseCase,
                          JoinCallUseCase joinCallUseCase,
                          LeaveCallUseCase leaveCallUseCase,
                          GetActiveCallUseCase getActiveCallUseCase) {
        this.startCallUseCase = startCallUseCase;
        this.joinCallUseCase = joinCallUseCase;
        this.leaveCallUseCase = leaveCallUseCase;
        this.getActiveCallUseCase = getActiveCallUseCase;
    }

    @PostMapping("/channels/{channelId}")
    public ResponseEntity<CallResponse> startCall(@AuthenticationPrincipal UUID userId,
                                                   @PathVariable UUID channelId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(startCallUseCase.startCall(userId, channelId));
    }

    @PostMapping("/{callId}/join")
    public ResponseEntity<CallResponse> joinCall(@AuthenticationPrincipal UUID userId,
                                                  @PathVariable UUID callId) {
        return ResponseEntity.ok(joinCallUseCase.joinCall(userId, callId));
    }

    @DeleteMapping("/{callId}/leave")
    public ResponseEntity<Void> leaveCall(@AuthenticationPrincipal UUID userId,
                                           @PathVariable UUID callId) {
        leaveCallUseCase.leaveCall(userId, callId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/channels/{channelId}/active")
    public ResponseEntity<CallResponse> getActiveCall(@PathVariable UUID channelId) {
        Optional<CallResponse> call = getActiveCallUseCase.getActiveCall(channelId);
        return call.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.noContent().build());
    }
}
