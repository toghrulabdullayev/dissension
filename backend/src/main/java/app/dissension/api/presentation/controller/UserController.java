package app.dissension.api.presentation.controller;

import app.dissension.api.application.user.dto.UpdateProfileRequest;
import app.dissension.api.application.user.dto.UserResponse;
import app.dissension.api.application.user.usecase.GetUserUseCase;
import app.dissension.api.application.user.usecase.UpdateProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    public UserController(GetUserUseCase getUserUseCase, UpdateProfileUseCase updateProfileUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(getUserUseCase.getUser(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal UUID currentUserId,
                                                       @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(updateProfileUseCase.updateProfile(currentUserId, request));
    }
}
