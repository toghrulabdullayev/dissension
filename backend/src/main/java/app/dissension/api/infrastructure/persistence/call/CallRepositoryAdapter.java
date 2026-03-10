package app.dissension.api.infrastructure.persistence.call;

import app.dissension.api.domain.call.entity.Call;
import app.dissension.api.domain.call.repository.CallRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CallRepositoryAdapter implements CallRepository {

    private final JpaCallRepository jpa;

    public CallRepositoryAdapter(JpaCallRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<Call> findById(UUID id)                        { return jpa.findById(id); }
    @Override public Optional<Call> findActiveCallByChannelId(UUID channelId){ return jpa.findActiveCallByChannelId(channelId); }
    @Override public Call save(Call call)                                     { return jpa.save(call); }
}
