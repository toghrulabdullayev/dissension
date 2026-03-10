package app.dissension.api.application.server.usecase;

import app.dissension.api.application.server.dto.ServerMemberResponse;
import app.dissension.api.domain.server.valueobject.ServerRole;

import java.util.UUID;

public interface UpdateMemberRoleUseCase {
    ServerMemberResponse updateMemberRole(UUID actorId, UUID serverId, UUID targetUserId, ServerRole newRole);
}
