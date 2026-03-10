package app.dissension.api.application.server.usecase;

import java.util.UUID;

public interface KickMemberUseCase {
    void kickMember(UUID actorId, UUID serverId, UUID targetUserId);
}
