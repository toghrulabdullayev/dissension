package app.dissension.api.application.channel.service;

import app.dissension.api.application.channel.dto.ChannelResponse;
import app.dissension.api.application.channel.dto.CreateChannelRequest;
import app.dissension.api.application.channel.dto.UpdateChannelRequest;
import app.dissension.api.application.channel.usecase.CreateChannelUseCase;
import app.dissension.api.application.channel.usecase.DeleteChannelUseCase;
import app.dissension.api.application.channel.usecase.GetChannelsByServerUseCase;
import app.dissension.api.application.channel.usecase.UpdateChannelUseCase;
import app.dissension.api.application.exception.ForbiddenException;
import app.dissension.api.application.exception.ResourceNotFoundException;
import app.dissension.api.domain.channel.entity.Channel;
import app.dissension.api.domain.channel.repository.ChannelRepository;
import app.dissension.api.domain.server.entity.ServerMember;
import app.dissension.api.domain.server.repository.ServerMemberRepository;
import app.dissension.api.domain.server.repository.ServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChannelService implements CreateChannelUseCase, GetChannelsByServerUseCase,
        UpdateChannelUseCase, DeleteChannelUseCase {

    private final ChannelRepository channelRepository;
    private final ServerRepository serverRepository;
    private final ServerMemberRepository serverMemberRepository;

    public ChannelService(ChannelRepository channelRepository,
                          ServerRepository serverRepository,
                          ServerMemberRepository serverMemberRepository) {
        this.channelRepository = channelRepository;
        this.serverRepository = serverRepository;
        this.serverMemberRepository = serverMemberRepository;
    }

    @Override
    public ChannelResponse createChannel(UUID actorId, UUID serverId, CreateChannelRequest request) {
        requireServer(serverId);
        requireCanManageChannels(serverId, actorId);
        Channel channel = Channel.create(serverId, request.name(), request.type(), request.position());
        return ChannelResponse.from(channelRepository.save(channel));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsByServer(UUID serverId, UUID requesterId) {
        requireServer(serverId);
        requireMember(serverId, requesterId);
        return channelRepository.findAllByServerIdOrderByPosition(serverId)
                .stream().map(ChannelResponse::from).toList();
    }

    @Override
    public ChannelResponse updateChannel(UUID actorId, UUID channelId, UpdateChannelRequest request) {
        Channel channel = requireChannel(channelId);
        requireCanManageChannels(channel.getServerId(), actorId);

        if (request.name() != null && !request.name().isBlank()) {
            channel.rename(request.name());
        }
        if (request.position() != null) {
            channel.reorder(request.position());
        }
        return ChannelResponse.from(channelRepository.save(channel));
    }

    @Override
    public void deleteChannel(UUID actorId, UUID channelId) {
        Channel channel = requireChannel(channelId);
        requireCanManageChannels(channel.getServerId(), actorId);
        channelRepository.deleteById(channelId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void requireServer(UUID serverId) {
        if (serverRepository.findById(serverId).isEmpty()) {
            throw new ResourceNotFoundException("Server", serverId);
        }
    }

    private Channel requireChannel(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId));
    }

    private ServerMember requireMember(UUID serverId, UUID userId) {
        return serverMemberRepository.findByServerIdAndUserId(serverId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a member of this server"));
    }

    private void requireCanManageChannels(UUID serverId, UUID actorId) {
        ServerMember actor = requireMember(serverId, actorId);
        if (!actor.getRole().canManageChannels()) {
            throw new ForbiddenException("Requires MOD or OWNER role to manage channels");
        }
    }
}
