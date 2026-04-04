package app.dissension.demo.auth.dto;

public record AuthResponse(
    String token,
    String username
) {
}
