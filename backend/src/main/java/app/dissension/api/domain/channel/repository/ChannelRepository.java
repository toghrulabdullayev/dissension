package app.dissension.api.domain.channel.repository;

import app.dissension.api.domain.channel.entity.Channel;
import app.dissension.api.domain.channel.valueobject.ChannelType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {
    Optional<Channel> findById(UUID id);
    List<Channel> findAllByServerIdOrderByPosition(UUID serverId);
    List<Channel> findAllByServerIdAndType(UUID serverId, ChannelType type);
    Channel save(Channel channel);
    void deleteById(UUID id);
}
