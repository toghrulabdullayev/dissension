package app.dissension.api.application.realtime.port;

import java.util.UUID;

/**
 * Port for user presence management.
 * Implemented by infrastructure using Redis.
 */
public interface PresencePort {

    /** Mark a user as ONLINE. */
    void setOnline(UUID userId);

    /** Mark a user as AWAY. */
    void setAway(UUID userId);

    /** Mark a user as OFFLINE and remove their presence record. */
    void setOffline(UUID userId);

    /** Returns the current presence status string: ONLINE, AWAY, or OFFLINE. */
    String getStatus(UUID userId);
}
