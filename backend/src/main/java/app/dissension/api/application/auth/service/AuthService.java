package app.dissension.api.application.auth.service;

import app.dissension.api.application.auth.dto.AuthResponse;
import app.dissension.api.application.auth.dto.LoginRequest;
import app.dissension.api.application.auth.dto.RegisterRequest;
import app.dissension.api.application.auth.dto.TokenRefreshResponse;
import app.dissension.api.application.auth.port.JwtPort;
import app.dissension.api.application.auth.port.PasswordEncoderPort;
import app.dissension.api.application.auth.usecase.LoginLocalUserUseCase;
import app.dissension.api.application.auth.usecase.LogoutUseCase;
import app.dissension.api.application.auth.usecase.ProcessOAuthLoginUseCase;
import app.dissension.api.application.auth.usecase.RefreshAccessTokenUseCase;
import app.dissension.api.application.auth.usecase.RegisterLocalUserUseCase;
import app.dissension.api.application.exception.ConflictException;
import app.dissension.api.application.exception.ForbiddenException;
import app.dissension.api.application.user.dto.UserResponse;
import app.dissension.api.domain.auth.entity.RefreshToken;
import app.dissension.api.domain.auth.repository.RefreshTokenRepository;
import app.dissension.api.domain.auth.service.TokenDomainService;
import app.dissension.api.domain.auth.valueobject.TokenHash;
import app.dissension.api.domain.user.entity.User;
import app.dissension.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class AuthService implements RegisterLocalUserUseCase, LoginLocalUserUseCase,
        ProcessOAuthLoginUseCase, RefreshAccessTokenUseCase, LogoutUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtPort jwtPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenDomainService tokenDomainService = new TokenDomainService();

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtPort jwtPort,
                       PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtPort = jwtPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }
        String hash = passwordEncoderPort.encode(request.password());
        User user = userRepository.save(User.createLocalUser(request.username(), request.email(), hash));
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ForbiddenException("Invalid credentials"));

        if (user.getPasswordHash() == null ||
                !passwordEncoderPort.matches(request.password(), user.getPasswordHash())) {
            throw new ForbiddenException("Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse processOAuthLogin(String email, String displayName, String avatarUrl) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> {
                    String username = generateUniqueUsername(displayName);
                    return userRepository.save(User.createOAuthUser(username, email, avatarUrl));
                });
        return buildAuthResponse(user);
    }

    @Override
    public TokenRefreshResponse refreshToken(String rawRefreshToken) {
        TokenHash hash = tokenDomainService.hashToken(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash.value())
                .orElseThrow(() -> new ForbiddenException("Invalid refresh token"));

        if (!tokenDomainService.validateRefreshToken(stored, rawRefreshToken)) {
            throw new ForbiddenException("Refresh token expired or revoked");
        }

        // Rotate: revoke old, issue new
        stored.revoke();
        refreshTokenRepository.save(stored);

        String newRaw = tokenDomainService.generateRawToken();
        RefreshToken newToken = tokenDomainService.createRefreshToken(stored.getUserId(), newRaw);
        refreshTokenRepository.save(newToken);

        String accessToken = jwtPort.issueAccessToken(stored.getUserId());
        return new TokenRefreshResponse(accessToken, newRaw);
    }

    @Override
    public void logout(String rawRefreshToken) {
        TokenHash hash = tokenDomainService.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash.value()).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtPort.issueAccessToken(user.getId());
        String rawRefreshToken = tokenDomainService.generateRawToken();
        RefreshToken refreshToken = tokenDomainService.createRefreshToken(user.getId(), rawRefreshToken);
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken, rawRefreshToken, UserResponse.from(user));
    }

    private String generateUniqueUsername(String displayName) {
        String base = displayName.replaceAll("[^a-zA-Z0-9_.]", "").toLowerCase();
        if (base.length() < 2) base = "user" + base;
        if (base.length() > 24) base = base.substring(0, 24);
        if (!userRepository.existsByUsername(base)) return base;
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = base + ThreadLocalRandom.current().nextInt(100, 9999);
            if (!userRepository.existsByUsername(candidate)) return candidate;
        }
        return base + System.currentTimeMillis();
    }
}
