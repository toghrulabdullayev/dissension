package app.dissension.api.infrastructure.persistence.server;

import app.dissension.api.domain.server.entity.Server;
import app.dissension.api.domain.server.repository.ServerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServerRepositoryAdapter implements ServerRepository {

    private final JpaServerRepository jpa;

    public ServerRepositoryAdapter(JpaServerRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<Server> findById(UUID id)                    { return jpa.findById(id); }
    @Override public List<Server> findAllByMemberUserId(UUID userId)        { return jpa.findAllByMemberUserId(userId); }
    @Override public Server save(Server server)                            { return jpa.save(server); }
    @Override public void deleteById(UUID id)                              { jpa.deleteById(id); }
}
