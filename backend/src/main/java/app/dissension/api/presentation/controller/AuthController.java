package app.dissension.api.presentation.controller;

import app.dissension.api.application.auth.dto.AuthResponse;
import app.dissension.api.application.auth.dto.LoginRequest;
import app.dissension.api.application.auth.dto.RegisterRequest;
import app.dissension.api.application.auth.dto.TokenRefreshResponse;
import app.dissension.api.application.auth.usecase.LoginLocalUserUseCase;
import app.dissension.api.application.auth.usecase.LogoutUseCase;
import app.dissension.api.application.auth.usecase.RefreshAccessTokenUseCase;
import app.dissension.api.application.auth.usecase.RegisterLocalUserUseCase;
import app.dissension.api.presentation.dto.AccessTokenResponse;
import app.dissension.api.presentation.dto.AuthPublicResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int REFRESH_COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 days
    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final RegisterLocalUserUseCase registerUseCase;
    private final LoginLocalUserUseCase loginUseCase;
    private final RefreshAccessTokenUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(RegisterLocalUserUseCase registerUseCase,
                          LoginLocalUserUseCase loginUseCase,
                          RefreshAccessTokenUseCase refreshUseCase,
                          LogoutUseCase logoutUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthPublicResponse> register(@Valid @RequestBody RegisterRequest request,
                                                        HttpServletRequest httpRequest,
                                                        HttpServletResponse httpResponse) {
        AuthResponse result = registerUseCase.register(request);
        httpResponse.addCookie(buildRefreshCookie(result.rawRefreshToken(), httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthPublicResponse(result.accessToken(), result.user()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthPublicResponse> login(@Valid @RequestBody LoginRequest request,
                                                     HttpServletRequest httpRequest,
                                                     HttpServletResponse httpResponse) {
        AuthResponse result = loginUseCase.login(request);
        httpResponse.addCookie(buildRefreshCookie(result.rawRefreshToken(), httpRequest));
        return ResponseEntity.ok(new AuthPublicResponse(result.accessToken(), result.user()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String rawRefreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenRefreshResponse result = refreshUseCase.refreshToken(rawRefreshToken);
        httpResponse.addCookie(buildRefreshCookie(result.rawRefreshToken(), httpRequest));
        return ResponseEntity.ok(new AccessTokenResponse(result.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String rawRefreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            logoutUseCase.logout(rawRefreshToken);
        }
        httpResponse.addCookie(clearRefreshCookie(httpRequest));
        return ResponseEntity.noContent().build();
    }

    private Cookie buildRefreshCookie(String value, HttpServletRequest request) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(REFRESH_COOKIE_PATH);
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    private Cookie clearRefreshCookie(HttpServletRequest request) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(REFRESH_COOKIE_PATH);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
