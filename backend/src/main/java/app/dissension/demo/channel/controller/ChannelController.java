package app.dissension.demo.channel.controller;

import app.dissension.demo.channel.dto.ChannelResponse;
import app.dissension.demo.channel.dto.CreateChannelRequest;
import app.dissension.demo.channel.service.ChannelService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servers/{serverId}/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    public ResponseEntity<List<ChannelResponse>> getServerChannels(
        @PathVariable UUID serverId,
        Principal principal
    ) {
        List<ChannelResponse> channels = channelService.getChannelsForServer(serverId, principal.getName());
        return ResponseEntity.ok(channels);
    }

    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(
        @PathVariable UUID serverId,
        Principal principal,
        @Valid @RequestBody CreateChannelRequest request
    ) {
        ChannelResponse created = channelService.createChannel(serverId, principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(
        @PathVariable UUID serverId,
        @PathVariable UUID channelId,
        Principal principal,
        @Valid @RequestBody CreateChannelRequest request
    ) {
        ChannelResponse updated = channelService.updateChannel(serverId, channelId, principal.getName(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(
        @PathVariable UUID serverId,
        @PathVariable UUID channelId,
        Principal principal
    ) {
        channelService.deleteChannel(serverId, channelId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
