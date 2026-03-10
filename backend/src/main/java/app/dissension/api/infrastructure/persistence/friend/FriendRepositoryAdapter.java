package app.dissension.api.infrastructure.persistence.friend;

import app.dissension.api.domain.friend.entity.Friend;
import app.dissension.api.domain.friend.repository.FriendRepository;
import app.dissension.api.domain.friend.valueobject.FriendStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FriendRepositoryAdapter implements FriendRepository {

    private final JpaFriendRepository jpa;

    public FriendRepositoryAdapter(JpaFriendRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Optional<Friend> findById(UUID id)                                      { return jpa.findById(id); }
    @Override public Optional<Friend> findByUserIdAndFriendId(UUID userId, UUID friendId)    { return jpa.findByUserIdAndFriendId(userId, friendId); }
    @Override public List<Friend> findAllByUserIdAndStatus(UUID userId, FriendStatus status) { return jpa.findAllByUserIdAndStatus(userId, status); }
    @Override public Friend save(Friend friend)                                               { return jpa.save(friend); }
    @Override public void delete(Friend friend)                                               { jpa.delete(friend); }

    @Override
    public List<Friend> findAllAcceptedFriends(UUID userId) {
        return jpa.findAllAcceptedFriends(userId, FriendStatus.ACCEPTED);
    }
}
