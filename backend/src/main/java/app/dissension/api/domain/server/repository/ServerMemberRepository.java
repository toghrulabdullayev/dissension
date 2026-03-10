package app.dissension.api.domain.server.repository;

import app.dissension.api.domain.server.entity.ServerMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServerMemberRepository {
    Optional<ServerMember> findByServerIdAndUserId(UUID serverId, UUID userId);
    List<ServerMember> findAllByServerId(UUID serverId);
    boolean existsByServerIdAndUserId(UUID serverId, UUID userId);
    long countByServerId(UUID serverId);
    ServerMember save(ServerMember member);
    void delete(ServerMember member);
}
