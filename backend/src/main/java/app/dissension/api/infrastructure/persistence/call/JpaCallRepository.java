package app.dissension.api.infrastructure.persistence.call;

import app.dissension.api.domain.call.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaCallRepository extends JpaRepository<Call, UUID> {

    @Query("SELECT c FROM Call c WHERE c.channelId = :channelId AND c.endedAt IS NULL")
    Optional<Call> findActiveCallByChannelId(@Param("channelId") UUID channelId);
}
