package app.dissension.api.domain.channel.valueobject;

public enum ChannelType {
    TEXT,
    VOICE,
    VIDEO;

    public boolean isVoiceBased() {
        return this == VOICE || this == VIDEO;
    }
}
