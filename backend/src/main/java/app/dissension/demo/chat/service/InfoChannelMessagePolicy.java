package app.dissension.demo.chat.service;

import app.dissension.demo.channel.model.ChannelType;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.model.ServerRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InfoChannelMessagePolicy extends AbstractChannelMessagePolicy {

  @Override
  public boolean supports(ChannelType channelType) {
    return channelType == ChannelType.INFO;
  }

  @Override
  public void assertCanSend(ServerMembership membership) {
    if (membership.getRole() != ServerRole.OWNER && membership.getRole() != ServerRole.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners and admins can post in INFO channels");
    }
  }
}
