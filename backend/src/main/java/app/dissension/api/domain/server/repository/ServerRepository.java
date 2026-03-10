package app.dissension.api.domain.server.repository;

import app.dissension.api.domain.server.entity.Server;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServerRepository {
    Optional<Server> findById(UUID id);
    List<Server> findAllByMemberUserId(UUID userId);
    Server save(Server server);
    void deleteById(UUID id);
}
