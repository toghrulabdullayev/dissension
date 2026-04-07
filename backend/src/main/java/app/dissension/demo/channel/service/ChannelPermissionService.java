package app.dissension.demo.channel.service;

import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChannelPermissionService {

    public void assertCanCreateChannel(ServerMembership membership) {
        if (membership.getRole() == ServerRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to create channels");
        }
    }

    public void assertCanManageChannel(ServerMembership membership) {
        if (membership.getRole() != ServerRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners can manage channels");
        }
    }
}