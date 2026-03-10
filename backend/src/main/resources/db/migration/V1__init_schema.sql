-- =============================================================
-- Dissension — V1 Initial Schema
-- =============================================================
-- Flyway applies this once, in order. Never edit after applying.
-- Requires PostgreSQL 13+ (gen_random_uuid() is core in PG 13+).
-- =============================================================


-- =============================================================
-- ENUM TYPES
-- =============================================================

CREATE TYPE auth_provider_type    AS ENUM ('GOOGLE', 'LOCAL');
CREATE TYPE user_status_type      AS ENUM ('ONLINE', 'IDLE', 'DO_NOT_DISTURB', 'OFFLINE');
CREATE TYPE friend_status_type    AS ENUM ('PENDING', 'ACCEPTED', 'BLOCKED');
CREATE TYPE conversation_type     AS ENUM ('DIRECT', 'GROUP');
CREATE TYPE server_role_type      AS ENUM ('OWNER', 'MOD', 'MEMBER');
CREATE TYPE channel_type          AS ENUM ('TEXT', 'VOICE', 'VIDEO');
CREATE TYPE message_type          AS ENUM ('TEXT', 'VOICE', 'SYSTEM');
CREATE TYPE game_session_status   AS ENUM ('WAITING', 'ACTIVE', 'FINISHED');


-- =============================================================
-- TABLES
-- =============================================================

-- -------------------------------------------------------------
-- users
-- password_hash is nullable: OAuth-only accounts have no password.
-- Constraint ensures LOCAL accounts always have a password hash.
-- -------------------------------------------------------------
CREATE TABLE users (
    id            UUID                NOT NULL DEFAULT gen_random_uuid(),
    username      VARCHAR(32)         NOT NULL,
    email         VARCHAR(254)        NOT NULL,
    password_hash VARCHAR(255),
    auth_provider auth_provider_type  NOT NULL,
    avatar_url    TEXT,
    status        user_status_type    NOT NULL DEFAULT 'OFFLINE',
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users              PRIMARY KEY (id),
    CONSTRAINT uq_users_username     UNIQUE (username),
    CONSTRAINT uq_users_email        UNIQUE (email),
    CONSTRAINT chk_users_password    CHECK (
        auth_provider = 'GOOGLE' OR password_hash IS NOT NULL
    )
);

-- -------------------------------------------------------------
-- refresh_tokens
-- Raw token is NEVER stored — only the SHA-256 hash.
-- Revoked flag allows explicit invalidation without deletion.
-- -------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_refresh_tokens          PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash     UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user     FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- friends
-- Bidirectional relationship: (A→B) and (B→A) are separate rows.
-- Self-friend prevented by check constraint.
-- -------------------------------------------------------------
CREATE TABLE friends (
    id         UUID               NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID               NOT NULL,
    friend_id  UUID               NOT NULL,
    status     friend_status_type NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ        NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_friends               PRIMARY KEY (id),
    CONSTRAINT uq_friends_pair          UNIQUE (user_id, friend_id),
    CONSTRAINT chk_friends_no_self      CHECK (user_id <> friend_id),
    CONSTRAINT fk_friends_user          FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friends_friend        FOREIGN KEY (friend_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- conversations
-- Covers both 1-on-1 DMs (DIRECT) and group DMs (GROUP).
-- name is only relevant for GROUP type.
-- Server channels are NOT conversations — they live in channels.
-- -------------------------------------------------------------
CREATE TABLE conversations (
    id         UUID              NOT NULL DEFAULT gen_random_uuid(),
    type       conversation_type NOT NULL,
    name       VARCHAR(100),
    created_at TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_conversations PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- conversation_participants
-- Junction table linking users to their conversations.
-- -------------------------------------------------------------
CREATE TABLE conversation_participants (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL,
    user_id         UUID        NOT NULL,
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_conversation_participants     PRIMARY KEY (id),
    CONSTRAINT uq_conversation_participant      UNIQUE (conversation_id, user_id),
    CONSTRAINT fk_conv_participants_conv        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_participants_user        FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- servers
-- owner_id uses RESTRICT: the server owner cannot be deleted
-- without first transferring ownership or deleting the server.
-- -------------------------------------------------------------
CREATE TABLE servers (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    owner_id   UUID         NOT NULL,
    icon_url   TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_servers           PRIMARY KEY (id),
    CONSTRAINT fk_servers_owner     FOREIGN KEY (owner_id)
        REFERENCES users(id) ON DELETE RESTRICT
);

-- -------------------------------------------------------------
-- server_members
-- role = OWNER is set for the founding member at creation time.
-- A server always has exactly one OWNER in server_members.
-- -------------------------------------------------------------
CREATE TABLE server_members (
    id        UUID             NOT NULL DEFAULT gen_random_uuid(),
    server_id UUID             NOT NULL,
    user_id   UUID             NOT NULL,
    role      server_role_type NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_server_members        PRIMARY KEY (id),
    CONSTRAINT uq_server_member         UNIQUE (server_id, user_id),
    CONSTRAINT fk_server_members_server FOREIGN KEY (server_id)
        REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_server_members_user   FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- channels
-- Only belong to servers. DMs are handled by conversations.
-- position drives the display order in the sidebar.
-- -------------------------------------------------------------
CREATE TABLE channels (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    server_id  UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    type       channel_type NOT NULL,
    position   INTEGER      NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_channels          PRIMARY KEY (id),
    CONSTRAINT fk_channels_server   FOREIGN KEY (server_id)
        REFERENCES servers(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- messages
-- Exactly one of channel_id or conversation_id must be set.
-- author_id uses SET NULL so messages survive user deletion
-- (displayed as "Deleted User" in UI).
-- deleted_at enables soft-deletion ("Message deleted" placeholder).
-- content is TEXT (nullable for VOICE messages — audio is in voice_messages).
-- -------------------------------------------------------------
CREATE TABLE messages (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    channel_id      UUID,
    conversation_id UUID,
    author_id       UUID,
    content         TEXT,
    type            message_type NOT NULL DEFAULT 'TEXT',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    edited_at       TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_messages              PRIMARY KEY (id),
    CONSTRAINT chk_message_target       CHECK (
        (channel_id IS NOT NULL AND conversation_id IS NULL) OR
        (channel_id IS NULL      AND conversation_id IS NOT NULL)
    ),
    CONSTRAINT fk_messages_channel      FOREIGN KEY (channel_id)
        REFERENCES channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_author       FOREIGN KEY (author_id)
        REFERENCES users(id) ON DELETE SET NULL
);

-- -------------------------------------------------------------
-- message_reactions
-- One emoji per user per message (duplicate emoji = same reaction).
-- -------------------------------------------------------------
CREATE TABLE message_reactions (
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    message_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    emoji      VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_message_reactions         PRIMARY KEY (id),
    CONSTRAINT uq_reaction                  UNIQUE (message_id, user_id, emoji),
    CONSTRAINT fk_reactions_message         FOREIGN KEY (message_id)
        REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_reactions_user            FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- calls
-- Tied to a server channel (VOICE or VIDEO type).
-- ended_at NULL means the call is still active.
-- -------------------------------------------------------------
CREATE TABLE calls (
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    channel_id UUID        NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at   TIMESTAMPTZ,

    CONSTRAINT pk_calls             PRIMARY KEY (id),
    CONSTRAINT fk_calls_channel     FOREIGN KEY (channel_id)
        REFERENCES channels(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- call_participants
-- left_at NULL means the participant is still in the call.
-- -------------------------------------------------------------
CREATE TABLE call_participants (
    id        UUID        NOT NULL DEFAULT gen_random_uuid(),
    call_id   UUID        NOT NULL,
    user_id   UUID        NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at   TIMESTAMPTZ,

    CONSTRAINT pk_call_participants         PRIMARY KEY (id),
    CONSTRAINT fk_call_participants_call    FOREIGN KEY (call_id)
        REFERENCES calls(id) ON DELETE CASCADE,
    CONSTRAINT fk_call_participants_user    FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- voice_messages
-- One-to-one extension of messages for VOICE type messages.
-- duration is stored in seconds.
-- -------------------------------------------------------------
CREATE TABLE voice_messages (
    id         UUID    NOT NULL DEFAULT gen_random_uuid(),
    message_id UUID    NOT NULL,
    audio_url  TEXT    NOT NULL,
    duration   INTEGER NOT NULL,

    CONSTRAINT pk_voice_messages            PRIMARY KEY (id),
    CONSTRAINT uq_voice_message_message_id  UNIQUE (message_id),
    CONSTRAINT fk_voice_messages_message    FOREIGN KEY (message_id)
        REFERENCES messages(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- game_sessions
-- Tied to a server channel. status = FINISHED and ended_at set
-- once the game concludes.
-- -------------------------------------------------------------
CREATE TABLE game_sessions (
    id         UUID                NOT NULL DEFAULT gen_random_uuid(),
    channel_id UUID                NOT NULL,
    game_type  VARCHAR(100)        NOT NULL,
    status     game_session_status NOT NULL DEFAULT 'WAITING',
    started_at TIMESTAMPTZ,
    ended_at   TIMESTAMPTZ,

    CONSTRAINT pk_game_sessions             PRIMARY KEY (id),
    CONSTRAINT fk_game_sessions_channel     FOREIGN KEY (channel_id)
        REFERENCES channels(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- game_participants
-- score starts at 0 and is updated as the game progresses.
-- -------------------------------------------------------------
CREATE TABLE game_participants (
    id         UUID    NOT NULL DEFAULT gen_random_uuid(),
    session_id UUID    NOT NULL,
    user_id    UUID    NOT NULL,
    score      INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT pk_game_participants         PRIMARY KEY (id),
    CONSTRAINT uq_game_participant          UNIQUE (session_id, user_id),
    CONSTRAINT fk_game_participants_session FOREIGN KEY (session_id)
        REFERENCES game_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_participants_user    FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);


-- =============================================================
-- INDEXES
-- High-frequency query paths documented for each index.
-- Unique constraints already create indexes automatically.
-- =============================================================

-- refresh_tokens: find all tokens for a user (token rotation, revoke-all)
CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

-- friends: list all of a user's friends / find reverse relationship
CREATE INDEX idx_friends_user_id
    ON friends(user_id);
CREATE INDEX idx_friends_friend_id
    ON friends(friend_id);

-- conversation_participants: load a user's conversation list
CREATE INDEX idx_conv_participants_user_id
    ON conversation_participants(user_id);
-- conversation_participants: load all members of a conversation
CREATE INDEX idx_conv_participants_conv_id
    ON conversation_participants(conversation_id);

-- server_members: load all servers a user has joined
CREATE INDEX idx_server_members_user_id
    ON server_members(user_id);
-- server_members: load all members of a server (member list sidebar)
CREATE INDEX idx_server_members_server_id
    ON server_members(server_id);

-- channels: load ordered channel list for a server
CREATE INDEX idx_channels_server_position
    ON channels(server_id, position ASC);

-- messages: paginated history for a channel — newest first, excluding deleted
CREATE INDEX idx_messages_channel_history
    ON messages(channel_id, created_at DESC)
    WHERE channel_id IS NOT NULL AND deleted_at IS NULL;

-- messages: paginated history for a DM conversation — newest first, excluding deleted
CREATE INDEX idx_messages_conversation_history
    ON messages(conversation_id, created_at DESC)
    WHERE conversation_id IS NOT NULL AND deleted_at IS NULL;

-- messages: find all messages by a user (profile / moderation)
CREATE INDEX idx_messages_author_id
    ON messages(author_id);

-- message_reactions: load all reactions for a message (reaction bar)
CREATE INDEX idx_reactions_message_id
    ON message_reactions(message_id);

-- calls: find the currently active call in a channel
CREATE INDEX idx_calls_channel_active
    ON calls(channel_id)
    WHERE ended_at IS NULL;

-- call_participants: load all participants in a call
CREATE INDEX idx_call_participants_call_id
    ON call_participants(call_id);
-- call_participants: check if a user is currently in any call
CREATE INDEX idx_call_participants_user_id
    ON call_participants(user_id);

-- game_sessions: find the active game session in a channel
CREATE INDEX idx_game_sessions_channel_active
    ON game_sessions(channel_id)
    WHERE ended_at IS NULL;

-- game_participants: load all participants and scores for a session
CREATE INDEX idx_game_participants_session_id
    ON game_participants(session_id);
