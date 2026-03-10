package app.dissension.api.infrastructure.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Periodically scans Redis for typing keys that are about to expire and broadcasts
 * TYPING_STOP events so clients can clear their local typing indicators.
 *
 * Strategy: every 2 seconds, scan keys matching typing:* that have TTL ≤ 1 second.
 * Delete them and broadcast TYPING_STOP so the frontend stops showing the indicator.
 *
 * This avoids the complexity of Redis keyspace notifications while still providing
 * timely expiry notifications on a single-instance deployment.
 */
@Component
public class TypingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(TypingExpiryScheduler.class);

    private final StringRedisTemplate redis;
    private final StompEventPublisher publisher;

    public TypingExpiryScheduler(StringRedisTemplate redis, StompEventPublisher publisher) {
        this.redis = redis;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 2000)
    public void sweepExpiredTypingKeys() {
        ScanOptions options = ScanOptions.scanOptions()
                .match("typing:*")
                .count(200)
                .build();

        try (Cursor<String> cursor = redis.scan(options)) {
            cursor.forEachRemaining(key -> {
                Long ttl = redis.getExpire(key); // seconds
                if (ttl != null && ttl <= 1) {
                    handleExpiry(key);
                }
            });
        } catch (Exception ex) {
            log.warn("Error during typing key sweep", ex);
        }
    }

    private void handleExpiry(String key) {
        try {
            Boolean deleted = redis.delete(key);
            if (!Boolean.TRUE.equals(deleted)) {
                return; // already deleted / expired naturally
            }

            // key format:
            //   typing:channel:{channelId}:{userId}
            //   typing:conversation:{conversationId}:{userId}
            String[] parts = key.split(":");
            if (parts.length < 4) return;

            String contextType = parts[1]; // "channel" or "conversation"
            UUID targetId = UUID.fromString(parts[2]);
            UUID userId   = UUID.fromString(parts[3]);

            if ("channel".equals(contextType)) {
                publisher.publishTypingStop("/topic/channels/" + targetId, userId, targetId, null);
            } else if ("conversation".equals(contextType)) {
                publisher.publishTypingStop("/topic/conversations/" + targetId, userId, null, targetId);
            }
        } catch (Exception ex) {
            log.warn("Error processing expiry for typing key: {}", key, ex);
        }
    }
}
