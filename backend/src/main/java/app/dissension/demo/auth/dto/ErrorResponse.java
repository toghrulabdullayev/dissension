package app.dissension.demo.auth.dto;

import java.util.Map;

public record ErrorResponse(
    String message,
    Map<String, String> validationErrors
) {
}
