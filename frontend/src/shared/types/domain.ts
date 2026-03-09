// Shared domain types used across all FSD layers

export type UUID = string;

export type AuthProvider = 'GOOGLE' | 'LOCAL';

export type UserStatus = 'ONLINE' | 'IDLE' | 'DO_NOT_DISTURB' | 'OFFLINE';

export type ChannelType = 'TEXT' | 'VOICE' | 'VIDEO';

export type MessageType = 'TEXT' | 'VOICE' | 'SYSTEM';

export type ConversationType = 'DIRECT' | 'GROUP';

export type ServerRole = 'OWNER' | 'MOD' | 'MEMBER';

export type FriendStatus = 'PENDING' | 'ACCEPTED' | 'BLOCKED';

export type GameSessionStatus = 'WAITING' | 'ACTIVE' | 'FINISHED';
