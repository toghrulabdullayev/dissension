package app.dissension.demo.chat.controller;

import app.dissension.demo.chat.dto.ChatMessageResponse;
import app.dissension.demo.chat.dto.SendChatMessageRequest;
import app.dissension.demo.chat.service.ChatMessageService;
import app.dissension.demo.chat.socket.event.ChatEventPublisher;
import app.dissension.demo.chat.socket.event.ChannelMessageCreatedEvent;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servers/{serverId}/channels/{channelId}/messages")
public class ChatController {

  private final ChatMessageService chatMessageService;
  private final ChatEventPublisher chatEventPublisher;

  public ChatController(ChatMessageService chatMessageService, ChatEventPublisher chatEventPublisher) {
    this.chatMessageService = chatMessageService;
    this.chatEventPublisher = chatEventPublisher;
  }

  @GetMapping
  public ResponseEntity<List<ChatMessageResponse>> getMessages(
      @PathVariable UUID serverId,
      @PathVariable UUID channelId,
      Principal principal) {
    List<ChatMessageResponse> messages = chatMessageService.getRecentMessages(serverId, channelId, principal.getName());
    return ResponseEntity.ok(messages);
  }

  @PostMapping
  public ResponseEntity<ChatMessageResponse> sendMessage(
      @PathVariable UUID serverId,
      @PathVariable UUID channelId,
      Principal principal,
      @Valid @RequestBody SendChatMessageRequest request) {
    ChatMessageResponse created = chatMessageService.createMessage(serverId, channelId, principal.getName(), request.content());
    chatEventPublisher.publish(new ChannelMessageCreatedEvent(created));
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }
}
