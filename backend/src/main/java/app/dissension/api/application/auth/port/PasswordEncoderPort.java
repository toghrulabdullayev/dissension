package app.dissension.api.application.auth.port;

/**
 * Port interface for password encoding operations.
 * Implemented by the infrastructure security layer (BCrypt).
 */
public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
