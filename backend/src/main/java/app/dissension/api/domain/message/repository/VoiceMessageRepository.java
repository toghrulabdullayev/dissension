package app.dissension.api.domain.message.repository;

import app.dissension.api.domain.message.entity.VoiceMessage;

import java.util.Optional;
import java.util.UUID;

public interface VoiceMessageRepository {
    Optional<VoiceMessage> findByMessageId(UUID messageId);
    VoiceMessage save(VoiceMessage voiceMessage);
}
