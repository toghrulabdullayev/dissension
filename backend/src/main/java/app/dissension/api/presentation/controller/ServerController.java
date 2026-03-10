package app.dissension.api.presentation.controller;

import app.dissension.api.application.server.dto.CreateServerRequest;
import app.dissension.api.application.server.dto.ServerMemberResponse;
import app.dissension.api.application.server.dto.ServerResponse;
import app.dissension.api.application.server.dto.UpdateServerRequest;
import app.dissension.api.application.server.usecase.*;
import app.dissension.api.domain.server.valueobject.ServerRole;
import app.dissension.api.presentation.dto.UpdateMemberRoleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final CreateServerUseCase createServerUseCase;
    private final GetUserServersUseCase getUserServersUseCase;
    private final GetServerUseCase getServerUseCase;
    private final UpdateServerUseCase updateServerUseCase;
    private final DeleteServerUseCase deleteServerUseCase;
    private final JoinServerUseCase joinServerUseCase;
    private final LeaveServerUseCase leaveServerUseCase;
    private final KickMemberUseCase kickMemberUseCase;
    private final UpdateMemberRoleUseCase updateMemberRoleUseCase;

    public ServerController(CreateServerUseCase createServerUseCase,
                            GetUserServersUseCase getUserServersUseCase,
                            GetServerUseCase getServerUseCase,
                            UpdateServerUseCase updateServerUseCase,
                            DeleteServerUseCase deleteServerUseCase,
                            JoinServerUseCase joinServerUseCase,
                            LeaveServerUseCase leaveServerUseCase,
                            KickMemberUseCase kickMemberUseCase,
                            UpdateMemberRoleUseCase updateMemberRoleUseCase) {
        this.createServerUseCase = createServerUseCase;
        this.getUserServersUseCase = getUserServersUseCase;
        this.getServerUseCase = getServerUseCase;
        this.updateServerUseCase = updateServerUseCase;
        this.deleteServerUseCase = deleteServerUseCase;
        this.joinServerUseCase = joinServerUseCase;
        this.leaveServerUseCase = leaveServerUseCase;
        this.kickMemberUseCase = kickMemberUseCase;
        this.updateMemberRoleUseCase = updateMemberRoleUseCase;
    }

    @PostMapping
    public ResponseEntity<ServerResponse> createServer(@AuthenticationPrincipal UUID userId,
                                                        @Valid @RequestBody CreateServerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createServerUseCase.createServer(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<ServerResponse>> getUserServers(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(getUserServersUseCase.getUserServers(userId));
    }

    @GetMapping("/{serverId}")
    public ResponseEntity<ServerResponse> getServer(@AuthenticationPrincipal UUID userId,
                                                     @PathVariable UUID serverId) {
        return ResponseEntity.ok(getServerUseCase.getServer(serverId, userId));
    }

    @PutMapping("/{serverId}")
    public ResponseEntity<ServerResponse> updateServer(@AuthenticationPrincipal UUID userId,
                                                        @PathVariable UUID serverId,
                                                        @Valid @RequestBody UpdateServerRequest request) {
        return ResponseEntity.ok(updateServerUseCase.updateServer(userId, serverId, request));
    }

    @DeleteMapping("/{serverId}")
    public ResponseEntity<Void> deleteServer(@AuthenticationPrincipal UUID userId,
                                              @PathVariable UUID serverId) {
        deleteServerUseCase.deleteServer(userId, serverId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{serverId}/join")
    public ResponseEntity<ServerMemberResponse> joinServer(@AuthenticationPrincipal UUID userId,
                                                            @PathVariable UUID serverId) {
        return ResponseEntity.ok(joinServerUseCase.joinServer(userId, serverId));
    }

    @DeleteMapping("/{serverId}/leave")
    public ResponseEntity<Void> leaveServer(@AuthenticationPrincipal UUID userId,
                                             @PathVariable UUID serverId) {
        leaveServerUseCase.leaveServer(userId, serverId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{serverId}/members/{targetUserId}")
    public ResponseEntity<Void> kickMember(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID serverId,
                                            @PathVariable UUID targetUserId) {
        kickMemberUseCase.kickMember(userId, serverId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{serverId}/members/{targetUserId}/role")
    public ResponseEntity<ServerMemberResponse> updateMemberRole(@AuthenticationPrincipal UUID userId,
                                                                   @PathVariable UUID serverId,
                                                                   @PathVariable UUID targetUserId,
                                                                   @Valid @RequestBody UpdateMemberRoleRequest request) {
        ServerRole newRole = ServerRole.valueOf(request.role().toUpperCase());
        return ResponseEntity.ok(updateMemberRoleUseCase.updateMemberRole(userId, serverId, targetUserId, newRole));
    }
}
