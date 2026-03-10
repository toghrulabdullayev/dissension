package app.dissension.api.infrastructure.persistence.server;

import app.dissension.api.domain.server.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaServerRepository extends JpaRepository<Server, UUID> {

    @Query("SELECT DISTINCT s FROM Server s JOIN s.members m WHERE m.userId = :userId")
    List<Server> findAllByMemberUserId(@Param("userId") UUID userId);
}
