package app.dissension.api.presentation.controller;

import app.dissension.api.application.channel.dto.ChannelResponse;
import app.dissension.api.application.channel.dto.CreateChannelRequest;
import app.dissension.api.application.channel.dto.UpdateChannelRequest;
import app.dissension.api.application.channel.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servers/{serverId}/channels")
public class ChannelController {

    private final CreateChannelUseCase createChannelUseCase;
    private final GetChannelsByServerUseCase getChannelsByServerUseCase;
    private final UpdateChannelUseCase updateChannelUseCase;
    private final DeleteChannelUseCase deleteChannelUseCase;

    public ChannelController(CreateChannelUseCase createChannelUseCase,
                             GetChannelsByServerUseCase getChannelsByServerUseCase,
                             UpdateChannelUseCase updateChannelUseCase,
                             DeleteChannelUseCase deleteChannelUseCase) {
        this.createChannelUseCase = createChannelUseCase;
        this.getChannelsByServerUseCase = getChannelsByServerUseCase;
        this.updateChannelUseCase = updateChannelUseCase;
        this.deleteChannelUseCase = deleteChannelUseCase;
    }

    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(@AuthenticationPrincipal UUID userId,
                                                          @PathVariable UUID serverId,
                                                          @Valid @RequestBody CreateChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createChannelUseCase.createChannel(userId, serverId, request));
    }

    @GetMapping
    public ResponseEntity<List<ChannelResponse>> getChannels(@AuthenticationPrincipal UUID userId,
                                                              @PathVariable UUID serverId) {
        return ResponseEntity.ok(getChannelsByServerUseCase.getChannelsByServer(serverId, userId));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@AuthenticationPrincipal UUID userId,
                                                          @PathVariable UUID serverId,
                                                          @PathVariable UUID channelId,
                                                          @Valid @RequestBody UpdateChannelRequest request) {
        return ResponseEntity.ok(updateChannelUseCase.updateChannel(userId, channelId, request));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@AuthenticationPrincipal UUID userId,
                                               @PathVariable UUID serverId,
                                               @PathVariable UUID channelId) {
        deleteChannelUseCase.deleteChannel(userId, channelId);
        return ResponseEntity.noContent().build();
    }
}
