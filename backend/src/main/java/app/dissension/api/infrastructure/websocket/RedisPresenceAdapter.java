package app.dissension.api.infrastructure.websocket;

import app.dissension.api.application.realtime.port.PresencePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis-backed presence store.
 *
 * Key pattern: presence:{userId}
 * Value:       ONLINE | AWAY
 * TTL:         ONLINE = 6 hours (renewed on each WS heartbeat / reconnect)
 *              AWAY   = 30 minutes
 * Offline: key is deleted immediately.
 */
@Component
public class RedisPresenceAdapter implements PresencePort {

    private static final String KEY_PREFIX = "presence:";
    private static final Duration ONLINE_TTL = Duration.ofHours(6);
    private static final Duration AWAY_TTL   = Duration.ofMinutes(30);

    private final StringRedisTemplate redis;

    public RedisPresenceAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void setOnline(UUID userId) {
        redis.opsForValue().set(key(userId), "ONLINE", ONLINE_TTL);
    }

    @Override
    public void setAway(UUID userId) {
        redis.opsForValue().set(key(userId), "AWAY", AWAY_TTL);
    }

    @Override
    public void setOffline(UUID userId) {
        redis.delete(key(userId));
    }

    @Override
    public String getStatus(UUID userId) {
        String value = redis.opsForValue().get(key(userId));
        return value != null ? value : "OFFLINE";
    }

    private static String key(UUID userId) {
        return KEY_PREFIX + userId;
    }
}
