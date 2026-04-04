package app.dissension.demo.channel.repository;

import app.dissension.demo.channel.entity.AppChannel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppChannelRepository extends JpaRepository<AppChannel, Long> {

    List<AppChannel> findByServerIdOrderByPositionAsc(Long serverId);

    long countByServerId(Long serverId);
}
