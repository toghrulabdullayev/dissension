package app.dissension.demo.auth.exception;

import app.dissension.demo.auth.dto.ErrorResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice // intercepts exceptions thrown by controller methods
public class ApiExceptionHandler {

  // handles exceptions thrown by @Bean validation (@Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new LinkedHashMap<>();

    // put them in a map (key = field, value = message)
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

    // wrap and return
    ErrorResponse response = new ErrorResponse("Validation failed", errors);
    return ResponseEntity.badRequest().body(response);
  }

  // handles a built-in Spring exception thrown from controllers (carries HTTP status and more)
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    ErrorResponse response = new ErrorResponse(ex.getReason(), Map.of());
    return ResponseEntity.status(status).body(response);
  }
}
