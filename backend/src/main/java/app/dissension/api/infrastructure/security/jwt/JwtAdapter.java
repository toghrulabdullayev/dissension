package app.dissension.api.infrastructure.security.jwt;

import app.dissension.api.application.auth.port.JwtPort;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtAdapter implements JwtPort {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiry-ms:900000}")
    private long expiryMs;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public String issueAccessToken(UUID userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiryMs))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public UUID extractUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return UUID.fromString(subject);
    }

    @Override
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
