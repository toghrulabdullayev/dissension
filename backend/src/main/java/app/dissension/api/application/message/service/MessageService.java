package app.dissension.api.application.message.service;

import app.dissension.api.application.exception.ForbiddenException;
import app.dissension.api.application.exception.ResourceNotFoundException;
import app.dissension.api.application.exception.ValidationException;
import app.dissension.api.application.message.dto.EditMessageRequest;
import app.dissension.api.application.message.dto.MessageResponse;
import app.dissension.api.application.message.dto.ReactionResponse;
import app.dissension.api.application.message.dto.SendMessageRequest;
import app.dissension.api.application.message.usecase.*;
import app.dissension.api.application.realtime.port.RealtimeEventPublisher;
import app.dissension.api.domain.channel.entity.Channel;
import app.dissension.api.domain.channel.repository.ChannelRepository;
import app.dissension.api.domain.conversation.repository.ConversationRepository;
import app.dissension.api.domain.message.entity.Message;
import app.dissension.api.domain.message.entity.MessageReaction;
import app.dissension.api.domain.message.repository.MessageReactionRepository;
import app.dissension.api.domain.message.repository.MessageRepository;
import app.dissension.api.domain.message.service.MessageDomainService;
import app.dissension.api.domain.server.repository.ServerMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MessageService implements SendChannelMessageUseCase, SendConversationMessageUseCase,
        GetChannelMessagesUseCase, GetConversationMessagesUseCase,
        EditMessageUseCase, DeleteMessageUseCase, AddReactionUseCase, RemoveReactionUseCase {

    private final MessageRepository messageRepository;
    private final MessageReactionRepository messageReactionRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final ChannelRepository channelRepository;
    private final ConversationRepository conversationRepository;
    private final MessageDomainService messageDomainService;
    private final RealtimeEventPublisher eventPublisher;

    public MessageService(MessageRepository messageRepository,
                          MessageReactionRepository messageReactionRepository,
                          ServerMemberRepository serverMemberRepository,
                          ChannelRepository channelRepository,
                          ConversationRepository conversationRepository,
                          RealtimeEventPublisher eventPublisher) {
        this.messageRepository = messageRepository;
        this.messageReactionRepository = messageReactionRepository;
        this.serverMemberRepository = serverMemberRepository;
        this.channelRepository = channelRepository;
        this.conversationRepository = conversationRepository;
        this.messageDomainService = new MessageDomainService(messageReactionRepository);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public MessageResponse sendChannelMessage(UUID senderId, UUID channelId, SendMessageRequest request) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId));
        if (!serverMemberRepository.existsByServerIdAndUserId(channel.getServerId(), senderId)) {
            throw new ForbiddenException("You are not a member of this channel's server");
        }
        Message message = Message.createChannelTextMessage(channelId, senderId, request.content());
        MessageResponse response = MessageResponse.from(messageRepository.save(message));
        eventPublisher.publishChannelMessageCreated(channelId, response);
        return response;
    }

    @Override
    public MessageResponse sendConversationMessage(UUID senderId, UUID conversationId, SendMessageRequest request) {
        if (!conversationRepository.isParticipant(conversationId, senderId)) {
            throw new ForbiddenException("You are not a participant in this conversation");
        }
        Message message = Message.createConversationTextMessage(conversationId, senderId, request.content());
        MessageResponse response = MessageResponse.from(messageRepository.save(message));
        eventPublisher.publishConversationMessageCreated(conversationId, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getChannelMessages(UUID requesterId, UUID channelId, int limit, int offset) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId));
        if (!serverMemberRepository.existsByServerIdAndUserId(channel.getServerId(), requesterId)) {
            throw new ForbiddenException("You are not a member of this channel's server");
        }
        return messageRepository.findByChannelId(channelId, limit, offset)
                .stream().map(MessageResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(UUID requesterId, UUID conversationId, int limit, int offset) {
        if (!conversationRepository.isParticipant(conversationId, requesterId)) {
            throw new ForbiddenException("You are not a participant in this conversation");
        }
        return messageRepository.findByConversationId(conversationId, limit, offset)
                .stream().map(MessageResponse::from).toList();
    }

    @Override
    public MessageResponse editMessage(UUID actorId, UUID messageId, EditMessageRequest request) {
        Message message = requireMessage(messageId);
        try {
            messageDomainService.assertCanEdit(message, actorId);
        } catch (SecurityException e) {
            throw new ForbiddenException(e.getMessage());
        }
        message.edit(request.content());
        MessageResponse response = MessageResponse.from(messageRepository.save(message));
        if (message.getChannelId() != null) {
            eventPublisher.publishChannelMessageUpdated(message.getChannelId(), response);
        } else if (message.getConversationId() != null) {
            eventPublisher.publishConversationMessageUpdated(message.getConversationId(), response);
        }
        return response;
    }

    @Override
    public void deleteMessage(UUID actorId, UUID messageId) {
        Message message = requireMessage(messageId);
        try {
            messageDomainService.assertCanDelete(message, actorId);
        } catch (SecurityException e) {
            throw new ForbiddenException(e.getMessage());
        }
        UUID channelId = message.getChannelId();
        UUID conversationId = message.getConversationId();
        message.softDelete();
        messageRepository.save(message);
        if (channelId != null) {
            eventPublisher.publishChannelMessageDeleted(channelId, messageId);
        } else if (conversationId != null) {
            eventPublisher.publishConversationMessageDeleted(conversationId, messageId);
        }
    }

    @Override
    public ReactionResponse addReaction(UUID userId, UUID messageId, String emoji) {
        if (emoji == null || emoji.isBlank()) {
            throw new ValidationException("Emoji must not be blank");
        }
        Message message = requireMessage(messageId);
        try {
            MessageReaction reaction = messageDomainService.addReaction(message, userId, emoji);
            ReactionResponse response = ReactionResponse.from(messageReactionRepository.save(reaction));
            eventPublisher.publishReactionAdded(message.getChannelId(), message.getConversationId(), response);
            return response;
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    public void removeReaction(UUID userId, UUID messageId, String emoji) {
        Message message = requireMessage(messageId);
        try {
            messageDomainService.removeReaction(message, userId, emoji);
            eventPublisher.publishReactionRemoved(
                    message.getChannelId(), message.getConversationId(), messageId, emoji, userId);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    private Message requireMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
    }
}
