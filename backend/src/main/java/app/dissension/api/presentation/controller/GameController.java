package app.dissension.api.presentation.controller;

import app.dissension.api.application.game.dto.CreateGameSessionRequest;
import app.dissension.api.application.game.dto.GameSessionResponse;
import app.dissension.api.application.game.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final CreateGameSessionUseCase createGameSessionUseCase;
    private final JoinGameSessionUseCase joinGameSessionUseCase;
    private final StartGameSessionUseCase startGameSessionUseCase;
    private final FinishGameSessionUseCase finishGameSessionUseCase;
    private final GetGameSessionUseCase getGameSessionUseCase;

    public GameController(CreateGameSessionUseCase createGameSessionUseCase,
                          JoinGameSessionUseCase joinGameSessionUseCase,
                          StartGameSessionUseCase startGameSessionUseCase,
                          FinishGameSessionUseCase finishGameSessionUseCase,
                          GetGameSessionUseCase getGameSessionUseCase) {
        this.createGameSessionUseCase = createGameSessionUseCase;
        this.joinGameSessionUseCase = joinGameSessionUseCase;
        this.startGameSessionUseCase = startGameSessionUseCase;
        this.finishGameSessionUseCase = finishGameSessionUseCase;
        this.getGameSessionUseCase = getGameSessionUseCase;
    }

    @PostMapping("/channels/{channelId}")
    public ResponseEntity<GameSessionResponse> createSession(@AuthenticationPrincipal UUID userId,
                                                              @PathVariable UUID channelId,
                                                              @Valid @RequestBody CreateGameSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createGameSessionUseCase.createGameSession(userId, channelId, request));
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<GameSessionResponse> joinSession(@AuthenticationPrincipal UUID userId,
                                                            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(joinGameSessionUseCase.joinGameSession(userId, sessionId));
    }

    @PostMapping("/{sessionId}/start")
    public ResponseEntity<GameSessionResponse> startSession(@AuthenticationPrincipal UUID userId,
                                                             @PathVariable UUID sessionId) {
        return ResponseEntity.ok(startGameSessionUseCase.startGameSession(userId, sessionId));
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<GameSessionResponse> finishSession(@AuthenticationPrincipal UUID userId,
                                                              @PathVariable UUID sessionId) {
        return ResponseEntity.ok(finishGameSessionUseCase.finishGameSession(userId, sessionId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<GameSessionResponse> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(getGameSessionUseCase.getGameSession(sessionId));
    }
}
