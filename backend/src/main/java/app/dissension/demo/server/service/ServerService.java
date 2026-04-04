package app.dissension.demo.server.service;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.auth.repository.AppUserRepository;
import app.dissension.demo.server.dto.CreateServerRequest;
import app.dissension.demo.server.dto.DiscoverServerResponse;
import app.dissension.demo.server.dto.ServerResponse;
import app.dissension.demo.server.entity.AppServer;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import app.dissension.demo.server.repository.AppServerRepository;
import app.dissension.demo.server.repository.ServerMembershipRepository;
import java.util.Comparator;
import java.util.List;
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
        return serverMembershipRepository.findByUserUsernameOrderByServerIdAsc(username)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscoverServerResponse> discoverServers(String query) {
        String normalizedQuery = query == null ? "" : query.trim();

        List<AppServer> servers = normalizedQuery.isEmpty()
            ? appServerRepository.findAllByOrderByIdAsc()
            : appServerRepository.searchByQuery(normalizedQuery);

        return servers.stream()
            .map(this::toDiscoverResponse)
            .sorted(
                Comparator.comparingLong(DiscoverServerResponse::members)
                    .reversed()
                    .thenComparing(DiscoverServerResponse::id)
            )
            .toList();
    }

    @Transactional(readOnly = true)
    public ServerMembership requireMembership(Long serverId, String username) {
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

    private DiscoverServerResponse toDiscoverResponse(AppServer server) {
        long members = serverMembershipRepository.countByServerId(server.getId());
        String owner = serverMembershipRepository.findByServerIdAndRole(server.getId(), ServerRole.OWNER)
            .map((membership) -> membership.getUser().getUsername())
            .orElse("Unknown");

        return new DiscoverServerResponse(
            server.getId(),
            server.getName(),
            server.getDescription(),
            owner,
            members,
            0L
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
