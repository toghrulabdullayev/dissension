package app.dissension.demo.chat.service;

import app.dissension.demo.channel.model.ChannelType;
import app.dissension.demo.server.entity.ServerMembership;

public abstract class AbstractChannelMessagePolicy {

  public abstract boolean supports(ChannelType channelType);

  public abstract void assertCanSend(ServerMembership membership);
}
