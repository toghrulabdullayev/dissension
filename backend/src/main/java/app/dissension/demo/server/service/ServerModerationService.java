package app.dissension.demo.server.service;

import app.dissension.demo.server.dto.ServerMemberResponse;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import app.dissension.demo.server.repository.ServerMembershipRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServerModerationService {

  private final ServerMembershipRepository serverMembershipRepository;
  private final ServerMembershipService serverMembershipService;

  public ServerModerationService(
      ServerMembershipRepository serverMembershipRepository,
      ServerMembershipService serverMembershipService) {
    this.serverMembershipRepository = serverMembershipRepository;
    this.serverMembershipService = serverMembershipService;
  }

  @Transactional(readOnly = true)
  public List<ServerMemberResponse> getServerMembers(UUID serverId, String username) {
    serverMembershipService.requireMembership(serverId, username);

    return serverMembershipRepository.findByServerIdOrderByIdAsc(serverId)
        .stream()
        .sorted(
            Comparator // compares enum values as int for sorting
                .comparingInt((ServerMembership membership) -> roleSortOrder(membership.getRole()))
                .thenComparing(
                    (ServerMembership membership) -> membership.getUser().getUsername(),
                    String.CASE_INSENSITIVE_ORDER))
        .map((membership) -> new ServerMemberResponse(
            membership.getUser().getUsername(),
            membership.getUser().getImageUrl(),
            membership.getRole()))
        .toList();
  }

  @Transactional
  public List<ServerMemberResponse> updateServerMemberRole(
      UUID serverId,
      String actorUsername,
      String targetUsername,
      ServerRole requestedRole) {
    if (requestedRole != ServerRole.ADMIN && requestedRole != ServerRole.USER) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ADMIN and USER roles are supported");
    }

    ServerMembership actorMembership = serverMembershipService.requireMembership(serverId, actorUsername);
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
    ServerMembership actorMembership = serverMembershipService.requireMembership(serverId, actorUsername);
    ServerMembership targetMembership = serverMembershipRepository
        .findByServerIdAndUserUsername(serverId, targetUsername)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

    ensureCanBan(actorMembership, targetMembership);

    serverMembershipRepository.delete(targetMembership);
    return getServerMembers(serverId, actorUsername);
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

  // returns int value for each enum value
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