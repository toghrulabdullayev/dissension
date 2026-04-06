package app.dissension.demo.auth.security;

import app.dissension.demo.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey signingKey;
  private final long expirationMinutes;

  public JwtService(JwtProperties jwtProperties) {
    this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    this.expirationMinutes = jwtProperties.getExpirationMinutes();
  }

  // generates token, stores username as a subject. Uses security.jwt.secret as a secret and sets the expiration
  public String generateToken(String username) {
    Instant now = Instant.now();

    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
        .signWith(signingKey)
        .compact();
  }

  // decodes the JWT (receives username as a subject)
  public String extractUsername(String token) {
    return parseClaims(token).getSubject();
  }

  // checks if the token is correct and hasn't expired yet
  public boolean isTokenValid(String token, String username) {
    String tokenUsername = extractUsername(token);
    return tokenUsername.equals(username) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    Date expiration = parseClaims(token).getExpiration();
    return expiration.before(new Date());
  }

  // creates a parser instance and accepts tokens signed with my secret and verifies its signature
  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
