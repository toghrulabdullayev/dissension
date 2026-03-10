package app.dissension.api.domain.server.valueobject;

public enum ServerRole {
    OWNER,
    MOD,
    MEMBER;

    public boolean canManageChannels() {
        return this == OWNER || this == MOD;
    }

    public boolean canKickMembers() {
        return this == OWNER || this == MOD;
    }

    public boolean canManageServer() {
        return this == OWNER;
    }
}
