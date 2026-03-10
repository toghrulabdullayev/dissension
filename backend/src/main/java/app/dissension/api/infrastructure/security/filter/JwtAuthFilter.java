package app.dissension.api.infrastructure.security.filter;

import app.dissension.api.application.auth.port.JwtPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Reads the JWT from the Authorization: Bearer header, validates it,
 * and populates the SecurityContext so downstream code sees an authenticated principal.
 *
 * The principal set is the raw UUID of the user — controllers receive it via
 * {@code @AuthenticationPrincipal UUID userId}.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtPort jwtPort;

    public JwtAuthFilter(JwtPort jwtPort) {
        this.jwtPort = jwtPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtPort.validateAccessToken(token)) {
                UUID userId = jwtPort.extractUserId(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
