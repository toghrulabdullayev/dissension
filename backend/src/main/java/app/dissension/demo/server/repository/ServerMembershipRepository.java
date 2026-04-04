package app.dissension.demo.server.repository;

import app.dissension.demo.server.entity.ServerMembership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerMembershipRepository extends JpaRepository<ServerMembership, Long> {

    List<ServerMembership> findByUserUsernameOrderByServerIdAsc(String username);

    Optional<ServerMembership> findByServerIdAndUserUsername(Long serverId, String username);
}
