package app.dissension.api.application.realtime.event;

/**
 * All possible event types sent over the STOMP broker to subscribed clients.
 */
public enum WsEventType {
    // ---- Message events ----
    MESSAGE_CREATED,
    MESSAGE_UPDATED,
    MESSAGE_DELETED,

    // ---- Reaction events ----
    REACTION_ADDED,
    REACTION_REMOVED,

    // ---- Typing events ----
    TYPING_START,
    TYPING_STOP,

    // ---- Presence events ----
    PRESENCE_UPDATE
}
