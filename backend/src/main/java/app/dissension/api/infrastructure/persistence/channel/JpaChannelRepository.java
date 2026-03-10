package app.dissension.api.infrastructure.persistence.channel;

import app.dissension.api.domain.channel.entity.Channel;
import app.dissension.api.domain.channel.valueobject.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaChannelRepository extends JpaRepository<Channel, UUID> {

    @Query("SELECT c FROM Channel c WHERE c.serverId = :serverId ORDER BY c.position")
    List<Channel> findAllByServerIdOrderByPosition(@Param("serverId") UUID serverId);

    @Query("SELECT c FROM Channel c WHERE c.serverId = :serverId AND c.type = :type ORDER BY c.position")
    List<Channel> findAllByServerIdAndType(@Param("serverId") UUID serverId, @Param("type") ChannelType type);
}
