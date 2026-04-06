package app.dissension.demo.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated // enables validation such as @NotBlank, @Min and etc at STARTUP time
@ConfigurationProperties(prefix = "security.jwt") // binds security.jwt.* from application.properties
public class JwtProperties {

  @NotBlank // ensures the secret isn't null, empty or whitespace
  private String secret;

  @Min(1) // expiration-minutes >= 1 (kebab-case is converted to camelCase)
  private long expirationMinutes; // relaxed binding (supports multiple naming styles)

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getExpirationMinutes() {
    return expirationMinutes;
  }

  public void setExpirationMinutes(long expirationMinutes) {
    this.expirationMinutes = expirationMinutes;
  }
}
