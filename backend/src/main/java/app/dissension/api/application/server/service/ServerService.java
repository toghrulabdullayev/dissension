package app.dissension.api.application.server.service;

import app.dissension.api.application.exception.ConflictException;
import app.dissension.api.application.exception.ForbiddenException;
import app.dissension.api.application.exception.ResourceNotFoundException;
import app.dissension.api.application.server.dto.CreateServerRequest;
import app.dissension.api.application.server.dto.ServerMemberResponse;
import app.dissension.api.application.server.dto.ServerResponse;
import app.dissension.api.application.server.dto.UpdateServerRequest;
import app.dissension.api.application.server.usecase.CreateServerUseCase;
import app.dissension.api.application.server.usecase.DeleteServerUseCase;
import app.dissension.api.application.server.usecase.GetServerUseCase;
import app.dissension.api.application.server.usecase.GetUserServersUseCase;
import app.dissension.api.application.server.usecase.JoinServerUseCase;
import app.dissension.api.application.server.usecase.KickMemberUseCase;
import app.dissension.api.application.server.usecase.LeaveServerUseCase;
import app.dissension.api.application.server.usecase.UpdateMemberRoleUseCase;
import app.dissension.api.application.server.usecase.UpdateServerUseCase;
import app.dissension.api.domain.server.entity.Server;
import app.dissension.api.domain.server.entity.ServerMember;
import app.dissension.api.domain.server.repository.ServerMemberRepository;
import app.dissension.api.domain.server.repository.ServerRepository;
import app.dissension.api.domain.server.service.ServerDomainService;
import app.dissension.api.domain.server.valueobject.ServerRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ServerService implements CreateServerUseCase, GetServerUseCase, GetUserServersUseCase,
        UpdateServerUseCase, DeleteServerUseCase, JoinServerUseCase, LeaveServerUseCase,
        KickMemberUseCase, UpdateMemberRoleUseCase {

    private final ServerRepository serverRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final ServerDomainService serverDomainService;

    public ServerService(ServerRepository serverRepository, ServerMemberRepository serverMemberRepository) {
        this.serverRepository = serverRepository;
        this.serverMemberRepository = serverMemberRepository;
        this.serverDomainService = new ServerDomainService(serverMemberRepository);
    }

    @Override
    public ServerResponse createServer(UUID ownerId, CreateServerRequest request) {
        Server server = serverRepository.save(Server.create(request.name(), ownerId));
        ServerMember ownerMember = ServerMember.create(server, ownerId, ServerRole.OWNER);
        serverMemberRepository.save(ownerMember);
        return ServerResponse.from(server);
    }

    @Override
    @Transactional(readOnly = true)
    public ServerResponse getServer(UUID serverId, UUID requesterId) {
        Server server = requireServer(serverId);
        requireMember(serverId, requesterId);
        return ServerResponse.from(server);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServerResponse> getUserServers(UUID userId) {
        return serverRepository.findAllByMemberUserId(userId)
                .stream().map(ServerResponse::from).toList();
    }

    @Override
    public ServerResponse updateServer(UUID actorId, UUID serverId, UpdateServerRequest request) {
        Server server = requireServer(serverId);
        requireRole(serverId, actorId, ServerRole.OWNER);

        if (request.name() != null && !request.name().isBlank()) {
            server.updateName(request.name());
        }
        if (request.iconUrl() != null) {
            server.updateIcon(request.iconUrl());
        }
        return ServerResponse.from(serverRepository.save(server));
    }

    @Override
    public void deleteServer(UUID actorId, UUID serverId) {
        Server server = requireServer(serverId);
        requireRole(serverId, actorId, ServerRole.OWNER);
        serverRepository.deleteById(serverId);
    }

    @Override
    public ServerMemberResponse joinServer(UUID userId, UUID serverId) {
        Server server = requireServer(serverId);
        ServerMember member = serverDomainService.addMember(server, userId);
        return ServerMemberResponse.from(serverMemberRepository.save(member));
    }

    @Override
    public void leaveServer(UUID userId, UUID serverId) {
        Server server = requireServer(serverId);
        if (server.getOwnerId().equals(userId)) {
            throw new ConflictException("Server owner cannot leave — transfer ownership first");
        }
        serverDomainService.removeMember(server, userId);
    }

    @Override
    public void kickMember(UUID actorId, UUID serverId, UUID targetUserId) {
        Server server = requireServer(serverId);
        requireMinRole(serverId, actorId, ServerRole.MOD);
        serverDomainService.removeMember(server, targetUserId);
    }

    @Override
    public ServerMemberResponse updateMemberRole(UUID actorId, UUID serverId,
                                                  UUID targetUserId, ServerRole newRole) {
        requireServer(serverId);
        requireRole(serverId, actorId, ServerRole.OWNER);

        if (newRole == ServerRole.OWNER) {
            throw new ConflictException("Use transfer-ownership to assign the OWNER role");
        }
        ServerMember target = serverMemberRepository.findByServerIdAndUserId(serverId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in server"));

        if (newRole == ServerRole.MOD) target.promoteToMod();
        else target.demoteToMember();

        return ServerMemberResponse.from(serverMemberRepository.save(target));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Server requireServer(UUID serverId) {
        return serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server", serverId));
    }

    private ServerMember requireMember(UUID serverId, UUID userId) {
        return serverMemberRepository.findByServerIdAndUserId(serverId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a member of this server"));
    }

    private void requireRole(UUID serverId, UUID userId, ServerRole required) {
        ServerMember member = requireMember(serverId, userId);
        if (member.getRole() != required) {
            throw new ForbiddenException("Requires " + required + " role");
        }
    }

    private void requireMinRole(UUID serverId, UUID userId, ServerRole minimum) {
        ServerMember member = requireMember(serverId, userId);
        if (!member.getRole().canKickMembers() && minimum == ServerRole.MOD) {
            throw new ForbiddenException("Requires at least MOD role");
        }
    }
}
