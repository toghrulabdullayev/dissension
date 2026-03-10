package app.dissension.api.application.conversation.service;

import app.dissension.api.application.conversation.dto.ConversationResponse;
import app.dissension.api.application.conversation.dto.CreateGroupConversationRequest;
import app.dissension.api.application.conversation.usecase.CreateDirectConversationUseCase;
import app.dissension.api.application.conversation.usecase.CreateGroupConversationUseCase;
import app.dissension.api.application.conversation.usecase.GetUserConversationsUseCase;
import app.dissension.api.application.exception.ConflictException;
import app.dissension.api.domain.conversation.entity.Conversation;
import app.dissension.api.domain.conversation.entity.ConversationParticipant;
import app.dissension.api.domain.conversation.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConversationService implements CreateDirectConversationUseCase,
        CreateGroupConversationUseCase, GetUserConversationsUseCase {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public ConversationResponse createDirectConversation(UUID userId, UUID targetUserId) {
        conversationRepository.findDirectConversationBetween(userId, targetUserId).ifPresent(existing -> {
            throw new ConflictException("A direct conversation already exists between these users");
        });

        Conversation conversation = Conversation.createDirect();
        conversation = conversationRepository.save(conversation);

        ConversationParticipant p1 = ConversationParticipant.create(conversation, userId);
        ConversationParticipant p2 = ConversationParticipant.create(conversation, targetUserId);
        conversation.getParticipants().add(p1);
        conversation.getParticipants().add(p2);

        return ConversationResponse.from(conversation);
    }

    @Override
    public ConversationResponse createGroupConversation(UUID creatorId, CreateGroupConversationRequest request) {
        Conversation conversation = Conversation.createGroup(request.name());
        conversation = conversationRepository.save(conversation);

        List<UUID> allParticipants = new ArrayList<>(request.participantIds());
        if (!allParticipants.contains(creatorId)) {
            allParticipants.add(0, creatorId);
        }

        for (UUID participantId : allParticipants) {
            ConversationParticipant participant = ConversationParticipant.create(conversation, participantId);
            conversation.getParticipants().add(participant);
        }

        return ConversationResponse.from(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(UUID userId) {
        return conversationRepository.findAllByParticipantUserId(userId)
                .stream().map(ConversationResponse::from).toList();
    }
}
