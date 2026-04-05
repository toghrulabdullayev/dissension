package app.dissension.demo.server.repository;

import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerMembershipRepository extends JpaRepository<ServerMembership, Long> {

    List<ServerMembership> findByUserUsernameOrderByIdAsc(String username);

    List<ServerMembership> findByServerIdOrderByIdAsc(UUID serverId);

    Optional<ServerMembership> findByServerIdAndUserUsername(UUID serverId, String username);

    Optional<ServerMembership> findByServerIdAndRole(UUID serverId, ServerRole role);

    Optional<ServerMembership> findFirstByServerIdAndRoleOrderByIdAsc(UUID serverId, ServerRole role);

    long countByServerId(UUID serverId);
}
