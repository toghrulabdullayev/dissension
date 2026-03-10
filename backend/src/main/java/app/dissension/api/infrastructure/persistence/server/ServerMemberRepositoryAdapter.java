package app.dissension.api.infrastructure.persistence.server;

import app.dissension.api.domain.server.entity.ServerMember;
import app.dissension.api.domain.server.repository.ServerMemberRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServerMemberRepositoryAdapter implements ServerMemberRepository {

    private final JpaServerMemberRepository jpa;

    public ServerMemberRepositoryAdapter(JpaServerMemberRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<ServerMember> findByServerIdAndUserId(UUID sid, UUID uid) { return jpa.findByServerIdAndUserId(sid, uid); }
    @Override public List<ServerMember> findAllByServerId(UUID serverId)                { return jpa.findAllByServerId(serverId); }
    @Override public boolean existsByServerIdAndUserId(UUID sid, UUID uid)              { return jpa.existsByServerIdAndUserId(sid, uid); }
    @Override public long countByServerId(UUID serverId)                               { return jpa.countByServerId(serverId); }
    @Override public ServerMember save(ServerMember member)                            { return jpa.save(member); }
    @Override public void delete(ServerMember member)                                  { jpa.delete(member); }
}
