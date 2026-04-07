package app.dissension.demo.channel.repository;

import app.dissension.demo.channel.entity.AppChannel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// First in the generic type is the Entity type, the second - its PK type
public interface AppChannelRepository extends JpaRepository<AppChannel, UUID> {

  // Spring Data JPA derives queries from method names automatically
  List<AppChannel> findByServerIdOrderByPositionAsc(UUID serverId);

  Optional<AppChannel> findByIdAndServerId(UUID channelId, UUID serverId);

  void deleteAllByServerId(UUID serverId);

  // tries to count by serverId first, if fails, tries server.id
  long countByServerId(UUID serverId);
}
