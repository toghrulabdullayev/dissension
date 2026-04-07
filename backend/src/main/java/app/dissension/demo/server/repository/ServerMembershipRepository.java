package app.dissension.demo.server.repository;

import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerMembershipRepository extends JpaRepository<ServerMembership, Long> {

  List<ServerMembership> findByUserUsernameOrderByIdAsc(String username);

  List<ServerMembership> findByServerIdOrderByIdAsc(UUID serverId);

  Optional<ServerMembership> findByServerIdAndUserUsername(UUID serverId, String username);

  Optional<ServerMembership> findByServerIdAndRole(UUID serverId, ServerRole role);

  Optional<ServerMembership> findFirstByServerIdAndRoleOrderByIdAsc(UUID serverId, ServerRole role);

  long countByServerId(UUID serverId);

  @Query("""
      select sm.user.username
      from ServerMembership sm
      where sm.server.id = :serverId
      order by sm.id asc
      """)
  List<String> findUsernamesByServerIdOrderByMembershipIdAsc(@Param("serverId") UUID serverId);

  @Query("""
      select sm.server.id
      from ServerMembership sm
      where sm.user.username = :username
      order by sm.id asc
      """)
  List<UUID> findServerIdsByUsernameOrderByMembershipIdAsc(@Param("username") String username);
}
