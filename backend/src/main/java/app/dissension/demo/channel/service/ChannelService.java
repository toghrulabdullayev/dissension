package app.dissension.demo.channel.service;

import app.dissension.demo.channel.dto.ChannelResponse;
import app.dissension.demo.channel.dto.CreateChannelRequest;
import app.dissension.demo.channel.entity.AppChannel;
import app.dissension.demo.channel.repository.AppChannelRepository;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import app.dissension.demo.server.service.ServerService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChannelService {

    private final AppChannelRepository appChannelRepository;
    private final ServerService serverService;

    public ChannelService(AppChannelRepository appChannelRepository, ServerService serverService) {
        this.appChannelRepository = appChannelRepository;
        this.serverService = serverService;
    }

    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsForServer(Long serverId, String username) {
        serverService.requireMembership(serverId, username);

        return appChannelRepository.findByServerIdOrderByPositionAsc(serverId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ChannelResponse createChannel(Long serverId, String username, CreateChannelRequest request) {
        ServerMembership membership = serverService.requireMembership(serverId, username);

        if (membership.getRole() == ServerRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to create channels");
        }

        long nextPosition = appChannelRepository.countByServerId(serverId) + 1;
        AppChannel channel = new AppChannel(
            membership.getServer(),
            request.name().trim(),
            request.type(),
            (int) nextPosition
        );

        AppChannel saved = appChannelRepository.save(channel);
        return toResponse(saved);
    }

    private ChannelResponse toResponse(AppChannel channel) {
        return new ChannelResponse(
            channel.getId(),
            channel.getName(),
            channel.getType(),
            channel.getPosition()
        );
    }
}
