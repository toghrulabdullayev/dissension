package app.dissension.api.application.realtime.event;

/**
 * Generic envelope for all outbound WebSocket events.
 *
 * @param type    the event discriminator
 * @param payload the event-specific payload object
 */
public record WsEvent(WsEventType type, Object payload) {}
