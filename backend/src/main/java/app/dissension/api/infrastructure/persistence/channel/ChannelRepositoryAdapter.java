package app.dissension.api.infrastructure.persistence.channel;

import app.dissension.api.domain.channel.entity.Channel;
import app.dissension.api.domain.channel.repository.ChannelRepository;
import app.dissension.api.domain.channel.valueobject.ChannelType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChannelRepositoryAdapter implements ChannelRepository {

    private final JpaChannelRepository jpa;

    public ChannelRepositoryAdapter(JpaChannelRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<Channel> findById(UUID id)                                           { return jpa.findById(id); }
    @Override public List<Channel> findAllByServerIdOrderByPosition(UUID serverId)                 { return jpa.findAllByServerIdOrderByPosition(serverId); }
    @Override public List<Channel> findAllByServerIdAndType(UUID serverId, ChannelType type)       { return jpa.findAllByServerIdAndType(serverId, type); }
    @Override public Channel save(Channel channel)                                                 { return jpa.save(channel); }
    @Override public void deleteById(UUID id)                                                      { jpa.deleteById(id); }
}
