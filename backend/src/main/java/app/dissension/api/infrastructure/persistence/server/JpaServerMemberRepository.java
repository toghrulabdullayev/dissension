package app.dissension.api.infrastructure.persistence.server;

import app.dissension.api.domain.server.entity.ServerMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaServerMemberRepository extends JpaRepository<ServerMember, UUID> {

    @Query("SELECT m FROM ServerMember m WHERE m.server.id = :serverId AND m.userId = :userId")
    Optional<ServerMember> findByServerIdAndUserId(@Param("serverId") UUID serverId, @Param("userId") UUID userId);

    @Query("SELECT m FROM ServerMember m WHERE m.server.id = :serverId")
    List<ServerMember> findAllByServerId(@Param("serverId") UUID serverId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ServerMember m WHERE m.server.id = :serverId AND m.userId = :userId")
    boolean existsByServerIdAndUserId(@Param("serverId") UUID serverId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM ServerMember m WHERE m.server.id = :serverId")
    long countByServerId(@Param("serverId") UUID serverId);
}
