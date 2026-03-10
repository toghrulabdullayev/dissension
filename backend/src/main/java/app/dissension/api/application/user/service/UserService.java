package app.dissension.api.application.user.service;

import app.dissension.api.application.exception.ConflictException;
import app.dissension.api.application.exception.ResourceNotFoundException;
import app.dissension.api.application.user.dto.UpdateProfileRequest;
import app.dissension.api.application.user.dto.UserResponse;
import app.dissension.api.application.user.usecase.GetUserUseCase;
import app.dissension.api.application.user.usecase.UpdateProfileUseCase;
import app.dissension.api.domain.user.entity.User;
import app.dissension.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements GetUserUseCase, UpdateProfileUseCase {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID actorId, UpdateProfileRequest request) {
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        if (request.username() != null && !request.username().isBlank()) {
            String newUsername = request.username().strip();
            if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
                throw new ConflictException("Username already taken: " + newUsername);
            }
            user.updateUsername(newUsername);
        }

        if (request.avatarUrl() != null) {
            user.updateAvatar(request.avatarUrl());
        }

        return UserResponse.from(userRepository.save(user));
    }
}
