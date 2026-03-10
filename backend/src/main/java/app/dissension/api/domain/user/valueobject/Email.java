package app.dissension.api.domain.user.valueobject;

import java.util.regex.Pattern;

/**
 * Represents a validated, normalized email address.
 * Immutable value object — equality is based on the normalized value.
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        value = value.strip().toLowerCase();
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        if (value.length() > 254) {
            throw new IllegalArgumentException("Email must not exceed 254 characters");
        }
    }
}
