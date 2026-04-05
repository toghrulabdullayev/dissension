package app.dissension.demo.server.service;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.auth.repository.AppUserRepository;
import app.dissension.demo.channel.repository.AppChannelRepository;
import app.dissension.demo.server.dto.CreateServerRequest;
import app.dissension.demo.server.dto.DiscoverServerResponse;
import app.dissension.demo.server.dto.ServerMemberResponse;
import app.dissension.demo.server.dto.ServerResponse;
import app.dissension.demo.server.entity.AppServer;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import app.dissension.demo.server.repository.AppServerRepository;
import app.dissension.demo.server.repository.ServerMembershipRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServerService {

    private final AppServerRepository appServerRepository;
    private final AppChannelRepository appChannelRepository;
    private final ServerMembershipRepository serverMembershipRepository;
    private final AppUserRepository appUserRepository;

    public ServerService(
        AppServerRepository appServerRepository,
        AppChannelRepository appChannelRepository,
        ServerMembershipRepository serverMembershipRepository,
        AppUserRepository appUserRepository
    ) {
        this.appServerRepository = appServerRepository;
        this.appChannelRepository = appChannelRepository;
        this.serverMembershipRepository = serverMembershipRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public ServerResponse createServer(String username, CreateServerRequest request) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        AppServer server = new AppServer(request.name().trim(), normalizeDescription(request.description()));
        AppServer savedServer = appServerRepository.save(server);

        // Creator is always the owner of the new server.
        ServerMembership membership = new ServerMembership(savedServer, user, ServerRole.OWNER);
        serverMembershipRepository.save(membership);

        return toResponse(membership);
    }

    @Transactional(readOnly = true)
    public List<ServerResponse> getServersForUser(String username) {
        return serverMembershipRepository.findByUserUsernameOrderByIdAsc(username)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscoverServerResponse> discoverServers(String username, String query) {
        String normalizedQuery = query == null ? "" : query.trim();

        List<AppServer> servers = normalizedQuery.isEmpty()
            ? appServerRepository.findAllByOrderByIdAsc()
            : appServerRepository.searchByQuery(normalizedQuery);

        return servers.stream()
            .map((server) -> toDiscoverResponse(server, username))
            .sorted(
                Comparator.comparingLong(DiscoverServerResponse::members)
                    .reversed()
                    .thenComparing(DiscoverServerResponse::name, String.CASE_INSENSITIVE_ORDER)
            )
            .toList();
    }

    @Transactional
    public ServerResponse joinServer(UUID serverId, String username) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        ServerMembership existingMembership = serverMembershipRepository
            .findByServerIdAndUserUsername(serverId, username)
            .orElse(null);

        if (existingMembership != null) {
            return toResponse(existingMembership);
        }

        AppServer server = appServerRepository.findById(serverId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Server not found"));

        ServerMembership membership = new ServerMembership(server, user, ServerRole.USER);
        ServerMembership savedMembership = serverMembershipRepository.save(membership);
        return toResponse(savedMembership);
    }

    @Transactional
    public void leaveServer(UUID serverId, String username) {
        ServerMembership leavingMembership = requireMembership(serverId, username);
        long membersBeforeLeave = serverMembershipRepository.countByServerId(serverId);

        if (membersBeforeLeave == 1L) {
            serverMembershipRepository.delete(leavingMembership);
            appChannelRepository.deleteAllByServerId(serverId);
            appServerRepository.deleteById(serverId);
            return;
        }

        if (leavingMembership.getRole() == ServerRole.OWNER) {
            ServerMembership newOwner = serverMembershipRepository
                .findFirstByServerIdAndRoleOrderByIdAsc(serverId, ServerRole.ADMIN)
                .orElseGet(() -> serverMembershipRepository.findByServerIdOrderByIdAsc(serverId)
                    .stream()
                    .filter((membership) -> !membership.getUser().getUsername().equalsIgnoreCase(username))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Owner can leave only when at least one other member exists"
                    )));

            newOwner.setRole(ServerRole.OWNER);
            serverMembershipRepository.save(newOwner);
        }

        serverMembershipRepository.delete(leavingMembership);
    }

    @Transactional(readOnly = true)
    public List<ServerMemberResponse> getServerMembers(UUID serverId, String username) {
        requireMembership(serverId, username);

        return serverMembershipRepository.findByServerIdOrderByIdAsc(serverId)
            .stream()
            .sorted(
                Comparator
                    .comparingInt((ServerMembership membership) -> roleSortOrder(membership.getRole()))
                    .thenComparing(
                        (ServerMembership membership) -> membership.getUser().getUsername(),
                        String.CASE_INSENSITIVE_ORDER
                    )
            )
            .map((membership) -> new ServerMemberResponse(
                membership.getUser().getUsername(),
                membership.getUser().getImageUrl(),
                membership.getRole()
            ))
            .toList();
    }

    @Transactional
    public List<ServerMemberResponse> updateServerMemberRole(
        UUID serverId,
        String actorUsername,
        String targetUsername,
        ServerRole requestedRole
    ) {
        if (requestedRole != ServerRole.ADMIN && requestedRole != ServerRole.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ADMIN and USER roles are supported");
        }

        ServerMembership actorMembership = requireMembership(serverId, actorUsername);
        ServerMembership targetMembership = serverMembershipRepository
            .findByServerIdAndUserUsername(serverId, targetUsername)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        ensureCanUpdateRole(actorMembership, targetMembership);

        if (targetMembership.getRole() != requestedRole) {
            targetMembership.setRole(requestedRole);
            serverMembershipRepository.save(targetMembership);
        }

        return getServerMembers(serverId, actorUsername);
    }

    @Transactional
    public List<ServerMemberResponse> banServerMember(UUID serverId, String actorUsername, String targetUsername) {
        ServerMembership actorMembership = requireMembership(serverId, actorUsername);
        ServerMembership targetMembership = serverMembershipRepository
            .findByServerIdAndUserUsername(serverId, targetUsername)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        ensureCanBan(actorMembership, targetMembership);

        serverMembershipRepository.delete(targetMembership);
        return getServerMembers(serverId, actorUsername);
    }

    @Transactional(readOnly = true)
    public ServerMembership requireMembership(UUID serverId, String username) {
        return serverMembershipRepository.findByServerIdAndUserUsername(serverId, username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this server"));
    }

    private ServerResponse toResponse(ServerMembership membership) {
        AppServer server = membership.getServer();
        long members = serverMembershipRepository.countByServerId(server.getId());
        return new ServerResponse(
            server.getId(),
            server.getName(),
            server.getDescription(),
            members,
            membership.getRole()
        );
    }

    private DiscoverServerResponse toDiscoverResponse(AppServer server, String username) {
        long members = serverMembershipRepository.countByServerId(server.getId());
        String owner = serverMembershipRepository.findByServerIdAndRole(server.getId(), ServerRole.OWNER)
            .map((membership) -> membership.getUser().getUsername())
            .orElse("Unknown");
        boolean joined = serverMembershipRepository.findByServerIdAndUserUsername(server.getId(), username).isPresent();

        return new DiscoverServerResponse(
            server.getId(),
            server.getName(),
            server.getDescription(),
            owner,
            members,
            0L,
            joined
        );
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void ensureCanUpdateRole(ServerMembership actorMembership, ServerMembership targetMembership) {
        ServerRole actorRole = actorMembership.getRole();
        ServerRole targetRole = targetMembership.getRole();

        if (actorRole != ServerRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can change member roles");
        }

        if (actorMembership.getUser().getUsername().equalsIgnoreCase(targetMembership.getUser().getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot manage your own membership");
        }

        if (targetRole == ServerRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner membership cannot be modified");
        }

        if (targetRole != ServerRole.ADMIN && targetRole != ServerRole.USER && targetRole != ServerRole.MOD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported target role for role change");
        }
    }

    private void ensureCanBan(ServerMembership actorMembership, ServerMembership targetMembership) {
        ServerRole actorRole = actorMembership.getRole();
        ServerRole targetRole = targetMembership.getRole();

        if (actorRole != ServerRole.OWNER && actorRole != ServerRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to ban members");
        }

        if (actorMembership.getUser().getUsername().equalsIgnoreCase(targetMembership.getUser().getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot ban yourself");
        }

        if (targetRole == ServerRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner membership cannot be modified");
        }

        if (actorRole == ServerRole.ADMIN && targetRole != ServerRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins can only ban users");
        }
    }

    private int roleSortOrder(ServerRole role) {
        if (role == ServerRole.OWNER) {
            return 0;
        }

        if (role == ServerRole.ADMIN || role == ServerRole.MOD) {
            return 1;
        }

        return 2;
    }
}
