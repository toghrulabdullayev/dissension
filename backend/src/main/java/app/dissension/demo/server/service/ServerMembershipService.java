package app.dissension.demo.server.service;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.auth.repository.AppUserRepository;
import app.dissension.demo.channel.repository.AppChannelRepository;
import app.dissension.demo.chat.repository.ChatMessageRepository;
import app.dissension.demo.server.dto.ServerResponse;
import app.dissension.demo.server.entity.AppServer;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import app.dissension.demo.server.repository.AppServerRepository;
import app.dissension.demo.server.repository.ServerMembershipRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServerMembershipService {

  private final AppUserRepository appUserRepository;
  private final AppServerRepository appServerRepository;
  private final AppChannelRepository appChannelRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ServerMembershipRepository serverMembershipRepository;

  public ServerMembershipService(
      AppUserRepository appUserRepository,
      AppServerRepository appServerRepository,
      AppChannelRepository appChannelRepository,
      ChatMessageRepository chatMessageRepository,
      ServerMembershipRepository serverMembershipRepository) {
    this.appUserRepository = appUserRepository;
    this.appServerRepository = appServerRepository;
    this.appChannelRepository = appChannelRepository;
    this.chatMessageRepository = chatMessageRepository;
    this.serverMembershipRepository = serverMembershipRepository;
  }

  @Transactional
  public ServerResponse joinServer(UUID serverId, String username) {
    AppUser user = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    ServerMembership existingMembership = serverMembershipRepository
        .findByServerIdAndUserUsername(serverId, username)
        .orElse(null);

    // if already in server
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
      chatMessageRepository.deleteAllByChannelServerId(serverId);
      appChannelRepository.deleteAllByServerId(serverId);
      appServerRepository.deleteById(serverId);
      return;
    }

    // when OWNER leaves, closest ADMIN becomes OWNER, if no ADMINs then USER claims OWNERship
    if (leavingMembership.getRole() == ServerRole.OWNER) {
      ServerMembership newOwner = serverMembershipRepository
          .findFirstByServerIdAndRoleOrderByIdAsc(serverId, ServerRole.ADMIN)
          .orElseGet(() -> serverMembershipRepository.findByServerIdOrderByIdAsc(serverId)
              .stream()
              .filter((membership) -> !membership.getUser().getUsername().equalsIgnoreCase(username))
              .findFirst()
              .orElseThrow(() -> new ResponseStatusException(
                  HttpStatus.BAD_REQUEST,
                  "Owner can leave only when at least one other member exists")));

      newOwner.setRole(ServerRole.OWNER);
      serverMembershipRepository.save(newOwner);
    }

    serverMembershipRepository.delete(leavingMembership);
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
        membership.getRole());
  }
}