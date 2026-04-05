package app.dissension.demo.channel.repository;

import app.dissension.demo.channel.entity.AppChannel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppChannelRepository extends JpaRepository<AppChannel, UUID> {

    List<AppChannel> findByServerIdOrderByPositionAsc(UUID serverId);

    Optional<AppChannel> findByIdAndServerId(UUID channelId, UUID serverId);

    void deleteAllByServerId(UUID serverId);

    long countByServerId(UUID serverId);
}
