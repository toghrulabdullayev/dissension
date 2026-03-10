package app.dissension.api.infrastructure.security.oauth2;

import app.dissension.api.application.auth.dto.AuthResponse;
import app.dissension.api.application.auth.usecase.ProcessOAuthLoginUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Handles the OAuth2 callback after Google login:
 *  1. Calls the application layer to create/upsert the user.
 *  2. Sets the refresh token as an httpOnly cookie.
 *  3. Redirects to the frontend with the access token as a query parameter.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 30 * 24 * 60 * 60; // 30 days

    private final ProcessOAuthLoginUseCase processOAuthLoginUseCase;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    public OAuth2LoginSuccessHandler(ProcessOAuthLoginUseCase processOAuthLoginUseCase) {
        this.processOAuthLoginUseCase = processOAuthLoginUseCase;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        String email      = (String) attributes.get("email");
        String name       = (String) attributes.get("name");
        String avatarUrl  = (String) attributes.get("picture");

        AuthResponse authResponse = processOAuthLoginUseCase.processOAuthLogin(email, name, avatarUrl);

        // Refresh token as httpOnly cookie — only sent to the refresh endpoint
        Cookie refreshCookie = new Cookie("refreshToken", authResponse.rawRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(request.isSecure());  // Secure=true over HTTPS, false over HTTP (dev)
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(REFRESH_TOKEN_MAX_AGE_SECONDS);
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

        // Redirect frontend with access token in query param
        response.sendRedirect(redirectUri + "?token=" + authResponse.accessToken());
    }
}
