package app.dissension.api.application.game.service;

import app.dissension.api.application.exception.ConflictException;
import app.dissension.api.application.exception.ForbiddenException;
import app.dissension.api.application.exception.ResourceNotFoundException;
import app.dissension.api.application.game.dto.CreateGameSessionRequest;
import app.dissension.api.application.game.dto.GameSessionResponse;
import app.dissension.api.application.game.usecase.*;
import app.dissension.api.domain.game.entity.GameParticipant;
import app.dissension.api.domain.game.entity.GameSession;
import app.dissension.api.domain.game.repository.GameParticipantRepository;
import app.dissension.api.domain.game.repository.GameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class GameService implements CreateGameSessionUseCase, JoinGameSessionUseCase,
        StartGameSessionUseCase, FinishGameSessionUseCase, GetGameSessionUseCase {

    private final GameSessionRepository gameSessionRepository;
    private final GameParticipantRepository gameParticipantRepository;

    public GameService(GameSessionRepository gameSessionRepository,
                       GameParticipantRepository gameParticipantRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.gameParticipantRepository = gameParticipantRepository;
    }

    @Override
    public GameSessionResponse createGameSession(UUID hostId, UUID channelId, CreateGameSessionRequest request) {
        gameSessionRepository.findActiveSessionByChannelId(channelId).ifPresent(existing -> {
            throw new ConflictException("An active game session already exists in this channel");
        });

        GameSession session = GameSession.create(channelId, request.gameType());
        session = gameSessionRepository.save(session);

        GameParticipant host = GameParticipant.create(session, hostId);
        session.getParticipants().add(gameParticipantRepository.save(host));

        return GameSessionResponse.from(session);
    }

    @Override
    public GameSessionResponse joinGameSession(UUID userId, UUID sessionId) {
        GameSession session = requireSession(sessionId);
        if (gameParticipantRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new ConflictException("User is already a participant in this session");
        }
        GameParticipant participant = GameParticipant.create(session, userId);
        session.getParticipants().add(gameParticipantRepository.save(participant));
        return GameSessionResponse.from(session);
    }

    @Override
    public GameSessionResponse startGameSession(UUID actorId, UUID sessionId) {
        GameSession session = requireSession(sessionId);
        requireIsParticipant(sessionId, actorId, "Only a session participant can start the game");
        try {
            session.start();
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
        return GameSessionResponse.from(gameSessionRepository.save(session));
    }

    @Override
    public GameSessionResponse finishGameSession(UUID actorId, UUID sessionId) {
        GameSession session = requireSession(sessionId);
        requireIsParticipant(sessionId, actorId, "Only a session participant can finish the game");
        try {
            session.finish();
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
        return GameSessionResponse.from(gameSessionRepository.save(session));
    }

    @Override
    @Transactional(readOnly = true)
    public GameSessionResponse getGameSession(UUID sessionId) {
        return GameSessionResponse.from(requireSession(sessionId));
    }

    private GameSession requireSession(UUID sessionId) {
        return gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("GameSession", sessionId));
    }

    private void requireIsParticipant(UUID sessionId, UUID userId, String message) {
        if (!gameParticipantRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new ForbiddenException(message);
        }
    }
}
