package app.dissension.demo.auth.service;

import app.dissension.demo.auth.dto.AuthResponse;
import app.dissension.demo.auth.dto.LoginRequest;
import app.dissension.demo.auth.dto.SignupRequest;
import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.auth.repository.AppUserRepository;
import app.dissension.demo.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        String username = normalizeUsername(request.username());

        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (appUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        AppUser appUser = new AppUser(username, passwordEncoder.encode(request.password()));
        appUserRepository.save(appUser);

        String token = jwtService.generateToken(username);
        return new AuthResponse(token, username);
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());

        AppUser appUser = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), appUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(username);
        return new AuthResponse(token, username);
    }

    private String normalizeUsername(String username) {
        return username.trim();
    }
}
