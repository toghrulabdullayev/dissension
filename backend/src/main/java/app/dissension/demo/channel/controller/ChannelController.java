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

  // composition (dependency injection)
  private final ChannelService channelService;

  public ChannelController(ChannelService channelService) {
    this.channelService = channelService;
  }
  // ==================================

  @GetMapping // GET request
  public ResponseEntity<List<ChannelResponse>> getServerChannels(
      @PathVariable UUID serverId, // must match the {serverId}
      Principal principal) {
    List<ChannelResponse> channels = channelService.getChannelsForServer(serverId, principal.getName());
    return ResponseEntity.ok(channels); // status 200, equals .status(HttpStatus.OK).body(updated)
  }

  @PostMapping // POST request
  public ResponseEntity<ChannelResponse> createChannel(
      @PathVariable UUID serverId, // must match the {serverId}
      Principal principal, // principal represents the currently authenticated user
      @Valid @RequestBody CreateChannelRequest request) // @RequestBody converts JSON body in request into Java object 
  {
    // .getName() takes the JWT subject (here: username)
    ChannelResponse created = channelService.createChannel(serverId, principal.getName(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created); // status 201
  }

  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelResponse> updateChannel(
      @PathVariable UUID serverId,
      @PathVariable UUID channelId,
      Principal principal,
      @Valid @RequestBody CreateChannelRequest request) {
    ChannelResponse updated = channelService.updateChannel(serverId, channelId, principal.getName(), request);
    return ResponseEntity.ok(updated); // equals .status(HttpStatus.OK).body(updated)
  }

  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> deleteChannel(
      @PathVariable UUID serverId,
      @PathVariable UUID channelId,
      Principal principal) {
    channelService.deleteChannel(serverId, channelId, principal.getName());
    return ResponseEntity.noContent().build(); // status 204, has no body, should call .build()
  }
}
