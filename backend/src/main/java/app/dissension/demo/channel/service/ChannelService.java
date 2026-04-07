package app.dissension.demo.channel.service;

import app.dissension.demo.channel.dto.ChannelResponse;
import app.dissension.demo.channel.dto.CreateChannelRequest;
import app.dissension.demo.channel.entity.AppChannel;
import app.dissension.demo.channel.repository.AppChannelRepository;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.service.ServerMembershipService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChannelService {

    private final AppChannelRepository appChannelRepository;
    private final ServerMembershipService serverMembershipService;
    private final ChannelPermissionService channelPermissionService;

    public ChannelService(
        AppChannelRepository appChannelRepository,
        ServerMembershipService serverMembershipService,
        ChannelPermissionService channelPermissionService
    ) {
        this.appChannelRepository = appChannelRepository;
        this.serverMembershipService = serverMembershipService;
        this.channelPermissionService = channelPermissionService;
    }

    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsForServer(UUID serverId, String username) {
        serverMembershipService.requireMembership(serverId, username);

        return appChannelRepository.findByServerIdOrderByPositionAsc(serverId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ChannelResponse createChannel(UUID serverId, String username, CreateChannelRequest request) {
        ServerMembership membership = serverMembershipService.requireMembership(serverId, username);
        channelPermissionService.assertCanCreateChannel(membership);

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

    @Transactional
    public ChannelResponse updateChannel(
        UUID serverId,
        UUID channelId,
        String username,
        CreateChannelRequest request
    ) {
        ServerMembership membership = serverMembershipService.requireMembership(serverId, username);
        channelPermissionService.assertCanManageChannel(membership);

        AppChannel channel = appChannelRepository.findByIdAndServerId(channelId, serverId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found"));

        channel.setName(request.name().trim());
        channel.setType(request.type());

        AppChannel saved = appChannelRepository.save(channel);
        return toResponse(saved);
    }

    @Transactional
    public void deleteChannel(UUID serverId, UUID channelId, String username) {
        ServerMembership membership = serverMembershipService.requireMembership(serverId, username);
        channelPermissionService.assertCanManageChannel(membership);

        AppChannel channel = appChannelRepository.findByIdAndServerId(channelId, serverId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found"));

        appChannelRepository.delete(channel);
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
