package app.dissension.api.infrastructure.websocket;

import app.dissension.api.application.realtime.port.TypingPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis-backed typing indicator store.
 *
 * Key pattern: typing:channel:{channelId}:{userId}
 *              typing:conversation:{conversationId}:{userId}
 * TTL: 5 seconds — if the client is still typing it must refresh every ~3 s.
 *
 * After the TTL the key vanishes; the server scheduler in
 * {@link TypingExpiryScheduler} periodically sweeps and broadcasts TYPING_STOP
 * for any keys that have just expired.
 */
@Component
public class RedisTypingAdapter implements TypingPort {

    static final Duration TYPING_TTL = Duration.ofSeconds(5);
    static final String CHANNEL_PREFIX      = "typing:channel:";
    static final String CONVERSATION_PREFIX = "typing:conversation:";

    private final StringRedisTemplate redis;

    public RedisTypingAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean startTypingInChannel(UUID userId, UUID channelId) {
        String key = channelKey(channelId, userId);
        Boolean isNew = redis.opsForValue().setIfAbsent(key, "1", TYPING_TTL);
        if (Boolean.FALSE.equals(isNew)) {
            // Already typing — refresh TTL so it doesn't expire mid-sentence
            redis.expire(key, TYPING_TTL);
        }
        return Boolean.TRUE.equals(isNew);
    }

    @Override
    public boolean startTypingInConversation(UUID userId, UUID conversationId) {
        String key = conversationKey(conversationId, userId);
        Boolean isNew = redis.opsForValue().setIfAbsent(key, "1", TYPING_TTL);
        if (Boolean.FALSE.equals(isNew)) {
            redis.expire(key, TYPING_TTL);
        }
        return Boolean.TRUE.equals(isNew);
    }

    @Override
    public boolean stopTypingInChannel(UUID userId, UUID channelId) {
        Boolean deleted = redis.delete(channelKey(channelId, userId));
        return Boolean.TRUE.equals(deleted);
    }

    @Override
    public boolean stopTypingInConversation(UUID userId, UUID conversationId) {
        Boolean deleted = redis.delete(conversationKey(conversationId, userId));
        return Boolean.TRUE.equals(deleted);
    }

    // ---- Key builders (package-visible for scheduler) ----

    static String channelKey(UUID channelId, UUID userId) {
        return CHANNEL_PREFIX + channelId + ":" + userId;
    }

    static String conversationKey(UUID conversationId, UUID userId) {
        return CONVERSATION_PREFIX + conversationId + ":" + userId;
    }
}
