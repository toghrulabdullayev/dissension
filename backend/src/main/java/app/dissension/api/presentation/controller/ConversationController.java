package app.dissension.api.presentation.controller;

import app.dissension.api.application.conversation.dto.ConversationResponse;
import app.dissension.api.application.conversation.dto.CreateGroupConversationRequest;
import app.dissension.api.application.conversation.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final CreateDirectConversationUseCase createDirectConversationUseCase;
    private final CreateGroupConversationUseCase createGroupConversationUseCase;
    private final GetUserConversationsUseCase getUserConversationsUseCase;

    public ConversationController(CreateDirectConversationUseCase createDirectConversationUseCase,
                                  CreateGroupConversationUseCase createGroupConversationUseCase,
                                  GetUserConversationsUseCase getUserConversationsUseCase) {
        this.createDirectConversationUseCase = createDirectConversationUseCase;
        this.createGroupConversationUseCase = createGroupConversationUseCase;
        this.getUserConversationsUseCase = getUserConversationsUseCase;
    }

    @PostMapping("/direct/{targetUserId}")
    public ResponseEntity<ConversationResponse> createDirect(@AuthenticationPrincipal UUID userId,
                                                              @PathVariable UUID targetUserId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createDirectConversationUseCase.createDirectConversation(userId, targetUserId));
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationResponse> createGroup(@AuthenticationPrincipal UUID userId,
                                                             @Valid @RequestBody CreateGroupConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createGroupConversationUseCase.createGroupConversation(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getUserConversations(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(getUserConversationsUseCase.getUserConversations(userId));
    }
}
