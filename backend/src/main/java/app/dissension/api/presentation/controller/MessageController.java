package app.dissension.api.presentation.controller;

import app.dissension.api.application.message.dto.*;
import app.dissension.api.application.message.usecase.*;
import app.dissension.api.presentation.dto.AddReactionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final SendChannelMessageUseCase sendChannelMessageUseCase;
    private final GetChannelMessagesUseCase getChannelMessagesUseCase;
    private final SendConversationMessageUseCase sendConversationMessageUseCase;
    private final GetConversationMessagesUseCase getConversationMessagesUseCase;
    private final EditMessageUseCase editMessageUseCase;
    private final DeleteMessageUseCase deleteMessageUseCase;
    private final AddReactionUseCase addReactionUseCase;
    private final RemoveReactionUseCase removeReactionUseCase;

    public MessageController(SendChannelMessageUseCase sendChannelMessageUseCase,
                             GetChannelMessagesUseCase getChannelMessagesUseCase,
                             SendConversationMessageUseCase sendConversationMessageUseCase,
                             GetConversationMessagesUseCase getConversationMessagesUseCase,
                             EditMessageUseCase editMessageUseCase,
                             DeleteMessageUseCase deleteMessageUseCase,
                             AddReactionUseCase addReactionUseCase,
                             RemoveReactionUseCase removeReactionUseCase) {
        this.sendChannelMessageUseCase = sendChannelMessageUseCase;
        this.getChannelMessagesUseCase = getChannelMessagesUseCase;
        this.sendConversationMessageUseCase = sendConversationMessageUseCase;
        this.getConversationMessagesUseCase = getConversationMessagesUseCase;
        this.editMessageUseCase = editMessageUseCase;
        this.deleteMessageUseCase = deleteMessageUseCase;
        this.addReactionUseCase = addReactionUseCase;
        this.removeReactionUseCase = removeReactionUseCase;
    }

    @PostMapping("/channels/{channelId}")
    public ResponseEntity<MessageResponse> sendChannelMessage(@AuthenticationPrincipal UUID userId,
                                                               @PathVariable UUID channelId,
                                                               @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sendChannelMessageUseCase.sendChannelMessage(userId, channelId, request));
    }

    @GetMapping("/channels/{channelId}")
    public ResponseEntity<List<MessageResponse>> getChannelMessages(@AuthenticationPrincipal UUID userId,
                                                                     @PathVariable UUID channelId,
                                                                     @RequestParam(defaultValue = "50") int limit,
                                                                     @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(getChannelMessagesUseCase.getChannelMessages(userId, channelId, limit, offset));
    }

    @PostMapping("/conversations/{conversationId}")
    public ResponseEntity<MessageResponse> sendConversationMessage(@AuthenticationPrincipal UUID userId,
                                                                    @PathVariable UUID conversationId,
                                                                    @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sendConversationMessageUseCase.sendConversationMessage(userId, conversationId, request));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(@AuthenticationPrincipal UUID userId,
                                                                          @PathVariable UUID conversationId,
                                                                          @RequestParam(defaultValue = "50") int limit,
                                                                          @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(getConversationMessagesUseCase.getConversationMessages(userId, conversationId, limit, offset));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(@AuthenticationPrincipal UUID userId,
                                                        @PathVariable UUID messageId,
                                                        @Valid @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(editMessageUseCase.editMessage(userId, messageId, request));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@AuthenticationPrincipal UUID userId,
                                               @PathVariable UUID messageId) {
        deleteMessageUseCase.deleteMessage(userId, messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<ReactionResponse> addReaction(@AuthenticationPrincipal UUID userId,
                                                         @PathVariable UUID messageId,
                                                         @Valid @RequestBody AddReactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addReactionUseCase.addReaction(userId, messageId, request.emoji()));
    }

    @DeleteMapping("/{messageId}/reactions")
    public ResponseEntity<Void> removeReaction(@AuthenticationPrincipal UUID userId,
                                                @PathVariable UUID messageId,
                                                @RequestParam String emoji) {
        removeReactionUseCase.removeReaction(userId, messageId, emoji);
        return ResponseEntity.noContent().build();
    }
}
