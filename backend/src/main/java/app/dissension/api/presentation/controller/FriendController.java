package app.dissension.api.presentation.controller;

import app.dissension.api.application.friend.dto.FriendResponse;
import app.dissension.api.application.friend.usecase.*;
import app.dissension.api.domain.friend.valueobject.FriendStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final SendFriendRequestUseCase sendFriendRequestUseCase;
    private final AcceptFriendRequestUseCase acceptFriendRequestUseCase;
    private final DeclineFriendRequestUseCase declineFriendRequestUseCase;
    private final RemoveFriendUseCase removeFriendUseCase;
    private final BlockUserUseCase blockUserUseCase;
    private final GetFriendsUseCase getFriendsUseCase;

    public FriendController(SendFriendRequestUseCase sendFriendRequestUseCase,
                            AcceptFriendRequestUseCase acceptFriendRequestUseCase,
                            DeclineFriendRequestUseCase declineFriendRequestUseCase,
                            RemoveFriendUseCase removeFriendUseCase,
                            BlockUserUseCase blockUserUseCase,
                            GetFriendsUseCase getFriendsUseCase) {
        this.sendFriendRequestUseCase = sendFriendRequestUseCase;
        this.acceptFriendRequestUseCase = acceptFriendRequestUseCase;
        this.declineFriendRequestUseCase = declineFriendRequestUseCase;
        this.removeFriendUseCase = removeFriendUseCase;
        this.blockUserUseCase = blockUserUseCase;
        this.getFriendsUseCase = getFriendsUseCase;
    }

    @PostMapping("/requests/{targetId}")
    public ResponseEntity<FriendResponse> sendRequest(@AuthenticationPrincipal UUID userId,
                                                        @PathVariable UUID targetId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sendFriendRequestUseCase.sendFriendRequest(userId, targetId));
    }

    @PostMapping("/requests/{requesterId}/accept")
    public ResponseEntity<FriendResponse> acceptRequest(@AuthenticationPrincipal UUID userId,
                                                         @PathVariable UUID requesterId) {
        return ResponseEntity.ok(acceptFriendRequestUseCase.acceptFriendRequest(userId, requesterId));
    }

    @DeleteMapping("/requests/{requesterId}/decline")
    public ResponseEntity<Void> declineRequest(@AuthenticationPrincipal UUID userId,
                                                @PathVariable UUID requesterId) {
        declineFriendRequestUseCase.declineFriendRequest(userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal UUID userId,
                                              @PathVariable UUID friendId) {
        removeFriendUseCase.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/block/{targetId}")
    public ResponseEntity<Void> blockUser(@AuthenticationPrincipal UUID userId,
                                           @PathVariable UUID targetId) {
        blockUserUseCase.blockUser(userId, targetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(@AuthenticationPrincipal UUID userId,
                                                            @RequestParam(defaultValue = "ACCEPTED") FriendStatus status) {
        return ResponseEntity.ok(getFriendsUseCase.getFriends(userId, status));
    }
}
