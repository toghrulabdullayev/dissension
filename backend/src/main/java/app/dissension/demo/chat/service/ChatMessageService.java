package app.dissension.demo.chat.service;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.auth.repository.AppUserRepository;
import app.dissension.demo.channel.entity.AppChannel;
import app.dissension.demo.channel.model.ChannelType;
import app.dissension.demo.channel.repository.AppChannelRepository;
import app.dissension.demo.chat.dto.ChatMessageResponse;
import app.dissension.demo.chat.entity.ChatMessage;
import app.dissension.demo.chat.repository.ChatMessageRepository;
import app.dissension.demo.server.entity.ServerMembership;
import app.dissension.demo.server.service.ServerMembershipService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatMessageService {

  private final AppChannelRepository appChannelRepository;
  private final AppUserRepository appUserRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ServerMembershipService serverMembershipService;
  private final List<AbstractChannelMessagePolicy> channelMessagePolicies;

  public ChatMessageService(
      AppChannelRepository appChannelRepository,
      AppUserRepository appUserRepository,
      ChatMessageRepository chatMessageRepository,
      ServerMembershipService serverMembershipService,
      List<AbstractChannelMessagePolicy> channelMessagePolicies) {
    this.appChannelRepository = appChannelRepository;
    this.appUserRepository = appUserRepository;
    this.chatMessageRepository = chatMessageRepository;
    this.serverMembershipService = serverMembershipService;
    this.channelMessagePolicies = channelMessagePolicies;
  }

  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getRecentMessages(UUID serverId, UUID channelId, String username) {
    serverMembershipService.requireMembership(serverId, username);
    requireTextChannel(serverId, channelId);

    List<ChatMessage> recentMessages = new ArrayList<>(
        chatMessageRepository.findTop100ByChannelIdOrderByCreatedAtDesc(channelId));
    recentMessages.sort((left, right) -> left.getCreatedAt().compareTo(right.getCreatedAt()));

    return recentMessages.stream().map(this::toResponse).toList();
  }

  @Transactional
  public ChatMessageResponse createMessage(UUID serverId, UUID channelId, String username, String content) {
    ServerMembership membership = serverMembershipService.requireMembership(serverId, username);
    AppChannel channel = requireTextChannel(serverId, channelId);

    // strategy pattern selects permission rules by channel type.
    resolvePolicy(channel.getType()).assertCanSend(membership);

    String sanitizedContent = sanitizeContent(content);
    AppUser author = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    ChatMessage message = new ChatMessage(channel, author, sanitizedContent, Instant.now());
    ChatMessage saved = chatMessageRepository.save(message);
    return toResponse(saved);
  }

  private AppChannel requireTextChannel(UUID serverId, UUID channelId) {
    AppChannel channel = appChannelRepository.findByIdAndServerId(channelId, serverId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found"));

    if (channel.getType() == ChannelType.CALL) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Messaging is available only in INFO and CHAT channels");
    }

    return channel;
  }

  private AbstractChannelMessagePolicy resolvePolicy(ChannelType channelType) {
    return channelMessagePolicies.stream()
        .filter((policy) -> policy.supports(channelType))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported channel type"));
  }

  private String sanitizeContent(String content) {
    String normalized = content == null ? "" : content.trim();
    if (normalized.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
    }

    if (normalized.length() > 2000) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message must be at most 2000 characters");
    }

    return normalized;
  }

  private ChatMessageResponse toResponse(ChatMessage message) {
    return new ChatMessageResponse(
        message.getId(),
        message.getChannel().getServer().getId(),
        message.getChannel().getId(),
        message.getAuthor().getUsername(),
        message.getAuthor().getImageUrl(),
        message.getContent(),
        message.getCreatedAt().toString());
  }
}
