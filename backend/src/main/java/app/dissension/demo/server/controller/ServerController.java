package app.dissension.demo.server.controller;

import app.dissension.demo.server.dto.CreateServerRequest;
import app.dissension.demo.server.dto.DiscoverServerResponse;
import app.dissension.demo.server.dto.ServerMemberResponse;
import app.dissension.demo.server.dto.ServerResponse;
import app.dissension.demo.server.service.ServerService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<ServerResponse>> getMyServers(Principal principal) {
        List<ServerResponse> servers = serverService.getServersForUser(principal.getName());
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/discover")
    public ResponseEntity<List<DiscoverServerResponse>> discoverServers(
        Principal principal,
        @RequestParam(name = "query", required = false) String query
    ) {
        List<DiscoverServerResponse> servers = serverService.discoverServers(principal.getName(), query);
        return ResponseEntity.ok(servers);
    }

    @PostMapping("/{serverId}/join")
    public ResponseEntity<ServerResponse> joinServer(@PathVariable UUID serverId, Principal principal) {
        ServerResponse joined = serverService.joinServer(serverId, principal.getName());
        return ResponseEntity.ok(joined);
    }

    @GetMapping("/{serverId}/members")
    public ResponseEntity<List<ServerMemberResponse>> getServerMembers(
        @PathVariable UUID serverId,
        Principal principal
    ) {
        List<ServerMemberResponse> members = serverService.getServerMembers(serverId, principal.getName());
        return ResponseEntity.ok(members);
    }

    @PostMapping
    public ResponseEntity<ServerResponse> createServer(
        Principal principal,
        @Valid @RequestBody CreateServerRequest request
    ) {
        ServerResponse created = serverService.createServer(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
