package app.dissension.api.domain.server.service;

import app.dissension.api.domain.server.entity.Server;
import app.dissension.api.domain.server.entity.ServerMember;
import app.dissension.api.domain.server.repository.ServerMemberRepository;
import app.dissension.api.domain.server.valueobject.ServerRole;

import java.util.UUID;

/**
 * Domain service for Server aggregate operations that span multiple entities
 * or require coordination between entities.
 *
 * No Spring annotations — instantiated by the application layer.
 */
public class ServerDomainService {

    private final ServerMemberRepository serverMemberRepository;

    public ServerDomainService(ServerMemberRepository serverMemberRepository) {
        this.serverMemberRepository = serverMemberRepository;
    }

    /**
     * Creates a new ServerMember entity for the given user and server.
     * The caller is responsible for persisting the returned entity.
     */
    public ServerMember addMember(Server server, UUID userId) {
        if (serverMemberRepository.existsByServerIdAndUserId(server.getId(), userId)) {
            throw new IllegalStateException("User is already a member of this server");
        }
        return ServerMember.create(server, userId, ServerRole.MEMBER);
    }

    /**
     * Removes a member from the server.
     * Throws if the target user is the server owner (must transfer ownership first).
     */
    public void removeMember(Server server, UUID targetUserId) {
        if (server.getOwnerId().equals(targetUserId)) {
            throw new IllegalStateException("Cannot remove the server owner — transfer ownership first");
        }
        ServerMember member = serverMemberRepository
                .findByServerIdAndUserId(server.getId(), targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this server"));
        serverMemberRepository.delete(member);
    }

    /**
     * Asserts that the acting user has owner-level permissions on the server.
     */
    public void assertCanManageServer(UUID actorUserId, Server server) {
        serverMemberRepository.findByServerIdAndUserId(server.getId(), actorUserId)
                .filter(m -> m.getRole().canManageServer())
                .orElseThrow(() -> new SecurityException("Insufficient permissions to manage this server"));
    }

    /**
     * Asserts that the acting user has at least mod-level permissions on the server.
     */
    public void assertCanManageChannels(UUID actorUserId, Server server) {
        serverMemberRepository.findByServerIdAndUserId(server.getId(), actorUserId)
                .filter(m -> m.getRole().canManageChannels())
                .orElseThrow(() -> new SecurityException("Insufficient permissions to manage channels in this server"));
    }
}
