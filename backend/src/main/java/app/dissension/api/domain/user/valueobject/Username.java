package app.dissension.api.domain.user.valueobject;

/**
 * Represents a validated username.
 * Allowed characters: letters, digits, underscores, dots, hyphens.
 * Length: 2–32 characters.
 */
public record Username(String value) {

    public Username {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        value = value.strip();
        if (value.length() < 2 || value.length() > 32) {
            throw new IllegalArgumentException("Username must be between 2 and 32 characters");
        }
        if (!value.matches("^[a-zA-Z0-9_.\\-]+$")) {
            throw new IllegalArgumentException(
                    "Username may only contain letters, digits, underscores, dots, and hyphens");
        }
    }
}
