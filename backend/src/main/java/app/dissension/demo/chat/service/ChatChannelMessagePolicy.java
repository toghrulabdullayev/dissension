package app.dissension.demo.chat.service;

import app.dissension.demo.channel.model.ChannelType;
import app.dissension.demo.server.entity.ServerMembership;
import org.springframework.stereotype.Component;

@Component
public class ChatChannelMessagePolicy extends AbstractChannelMessagePolicy {

  @Override
  public boolean supports(ChannelType channelType) {
    return channelType == ChannelType.CHAT;
  }

  @Override
  public void assertCanSend(ServerMembership membership) {
    // Any member can send to CHAT channels.
  }
}
