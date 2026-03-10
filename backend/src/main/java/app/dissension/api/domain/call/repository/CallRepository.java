package app.dissension.api.domain.call.repository;

import app.dissension.api.domain.call.entity.Call;

import java.util.Optional;
import java.util.UUID;

public interface CallRepository {
    Optional<Call> findById(UUID id);
    Optional<Call> findActiveCallByChannelId(UUID channelId);
    Call save(Call call);
}
