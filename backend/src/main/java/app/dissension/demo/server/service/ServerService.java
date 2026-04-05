package app.dissension.demo.server.service;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.auth.repository.AppUserRepository;
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
    private final ServerMembershipRepository serverMembershipRepository;
    private final AppUserRepository appUserRepository;

    public ServerService(
        AppServerRepository appServerRepository,
        ServerMembershipRepository serverMembershipRepository,
        AppUserRepository appUserRepository
    ) {
        this.appServerRepository = appServerRepository;
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

    @Transactional(readOnly = true)
    public List<ServerMemberResponse> getServerMembers(UUID serverId, String username) {
        requireMembership(serverId, username);

        return serverMembershipRepository.findByServerIdOrderByIdAsc(serverId)
            .stream()
            .map((membership) -> new ServerMemberResponse(
                membership.getUser().getUsername(),
                membership.getUser().getImageUrl(),
                membership.getRole()
            ))
            .toList();
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
}
