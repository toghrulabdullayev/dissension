# System Architecture

**Dissension — Discord-like Social Platform**

Features:

```
servers
channels
text chat
voice messages
voice calls
video calls
screen sharing
in-call games
presence
friends
```

---

# High-Level Architecture

```
React Frontend (Feature-Sliced Design)
        ↓
REST / WebSockets / WebRTC
        ↓
Spring Boot Backend (DDD)
        ↓
PostgreSQL
Redis
Object Storage
```

Communication:

```
REST → standard API requests
WebSockets (STOMP over SockJS) → realtime chat & presence
WebRTC + coturn TURN server → voice/video calls & screen sharing
```

---

# Frontend Architecture

**React + TypeScript + Feature-Sliced Design**

## Tech Stack

| Concern | Technology |
|---|---|
| Framework | React + TypeScript |
| State Management | Zustand |
| Data Fetching | TanStack Query |
| Routing | TanStack Router |
| WebSocket Client | STOMP over SockJS |
| UI Components | shadcn/ui (Tailwind CSS) |
| Forms | React Hook Form |

Feature-Sliced Design structures frontend code into standardized layers such as `app`, `pages`, `widgets`, `features`, `entities`, and `shared`, organizing the codebase around business domains and user interactions. ([fsd.how](https://fsd.how/docs/get-started/overview?utm_source=chatgpt.com))

## Project Structure

```
src
 ├ app
 │   ├ providers
 │   ├ router
 │   ├ store
 │   └ websocket
 │
 ├ pages
 │   ├ login
 │   ├ register
 │   ├ server
 │   ├ direct-messages
 │   └ call-room
 │
 ├ widgets
 │   ├ server-sidebar
 │   ├ channel-list
 │   ├ chat-window
 │   ├ message-list
 │   ├ call-controls
 │   ├ video-grid
 │   └ game-panel
 │
 ├ features
 │   ├ auth-login
 │   ├ auth-register
 │   ├ auth-oauth
 │   ├ send-message
 │   ├ edit-message
 │   ├ delete-message
 │   ├ add-reaction
 │   ├ join-channel
 │   ├ join-call
 │   ├ start-video-call
 │   ├ start-screen-share
 │   ├ record-voice-message
 │   ├ send-friend-request
 │   └ start-game
 │
 ├ entities
 │   ├ user
 │   ├ server
 │   ├ channel
 │   ├ message
 │   ├ conversation
 │   ├ call
 │   ├ friend
 │   └ game-session
 │
 └ shared
     ├ api
     ├ ui
     ├ hooks
     ├ config
     ├ lib
     └ types
```

---

# Backend Architecture

**Spring Boot + Domain-Driven Design**

## Tech Stack

| Concern | Technology |
|---|---|
| Security | Spring Security (OAuth2 Google + JWT) |
| WebSocket | Spring WebSocket (STOMP broker) |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |

```
backend
 ├ domain
 │   ├ user
 │   ├ auth
 │   ├ server
 │   ├ channel
 │   ├ message
 │   ├ conversation
 │   ├ call
 │   ├ friend
 │   └ game
 │
 ├ application
 │   ├ dto
 │   ├ service
 │   └ usecase
 │
 ├ infrastructure
 │   ├ persistence
 │   ├ security
 │   ├ websocket
 │   ├ storage
 │   └ config
 │
 └ presentation
     ├ rest
     └ websocket
```

---

# Domain Structure (DDD)

Example domain module:

```
domain/message
 ├ entity
 │   └ Message.java
 │
 ├ repository
 │   └ MessageRepository.java
 │
 └ service
     └ MessageDomainService.java
```

Example application layer:

```
application/message
 ├ dto
 │   ├ SendMessageRequest
 │   └ MessageResponse
 │
 ├ usecase
 │   ├ SendMessageUseCase
 │   ├ EditMessageUseCase
 │   └ DeleteMessageUseCase
 │
 └ service
     └ MessagingService
```

---

# Authentication

## Flow

```
1  User clicks "Continue with Google"
2  Spring Security OAuth2 redirects to Google
3  Google returns authorization code
4  Backend exchanges code for Google user profile
5  Backend creates or finds local user account
6  Backend issues JWT access token (15 min, returned in response body)
7  Backend sets refresh token as httpOnly cookie (30 days)
8  Client stores access token in Zustand memory only — never localStorage
9  On access token expiry, client calls POST /auth/refresh
10 Backend validates refresh token cookie, issues new access token
```

## Token Strategy

```
Access token   JWT · 15 min · Zustand in-memory (never localStorage)
Refresh token  opaque hash · 30 days · httpOnly cookie · stored hashed in DB
```

---

# Realtime Communication

## Messaging

```
React Client (STOMP.js over SockJS)
      ↓
Spring WebSocket (STOMP broker)
      ↓
Backend STOMP Message Handler
      ↓
Messaging Domain
```

Supported features:

```
text messages
message reactions
message editing
typing indicators
message deletion
```

---

## Voice / Video Calls

```
React
 ↓
WebSocket signaling (STOMP)
 ↓
Call Service
 ↓
WebRTC peer connections (NAT traversal via coturn)
```

Supported features:

```
voice calls
video calls
group calls
screen sharing
```

---

## Voice Messages

```
Client records audio
      ↓
Upload to backend
      ↓
Object storage
      ↓
Message contains audio URL
```

---

## In-Call Games

```
React Canvas / WebGL
       ↓
Game Session API
       ↓
Game Domain
```

Features:

```
matchmaking
game state synchronization
leaderboards
```

---

# Database Schema

## Users

```
users
-----
id (uuid)
username
email
password_hash (nullable — null for OAuth-only accounts)
auth_provider (GOOGLE, LOCAL)
avatar_url
status
created_at
```

---

## Refresh Tokens

```
refresh_tokens
--------------
id (uuid)
user_id (fk → users)
token_hash
expires_at
revoked
created_at
```

---

## Friends

```
friends
-------
id (uuid)
user_id (fk → users)
friend_id (fk → users)
status (PENDING, ACCEPTED, BLOCKED)
created_at
```

---

## Conversations

```
conversations
-------------
id (uuid)
type (DIRECT, GROUP)
created_at
```

---

## Conversation Participants

```
conversation_participants
-------------------------
id (uuid)
conversation_id (fk → conversations)
user_id (fk → users)
joined_at
```

---

## Servers

```
servers
-------
id
name
owner_id
icon_url
created_at
```

---

## Server Members

```
server_members
--------------
id (uuid)
server_id (fk → servers)
user_id (fk → users)
role (OWNER, MOD, MEMBER)
joined_at
```

---

## Channels

```
channels
--------
id
server_id
name
type
created_at
```

Types:

```
TEXT
VOICE
VIDEO
```

---

## Messages

```
messages
--------
id (uuid)
channel_id (fk → channels, nullable)
conversation_id (fk → conversations, nullable)
author_id (fk → users)
content
type
created_at
edited_at
deleted_at
```

Note: exactly one of channel_id or conversation_id must be set.

Types:

```
TEXT
VOICE
SYSTEM
```

---

## Message Reactions

```
message_reactions
-----------------
id
message_id
user_id
emoji
created_at
```

---

## Calls

```
calls
-----
id
channel_id
started_at
ended_at
```

---

## Call Participants

```
call_participants
-----------------
id
call_id
user_id
joined_at
left_at
```

---

## Voice Messages

```
voice_messages
--------------
id
message_id
audio_url
duration
```

---

## Game Sessions

```
game_sessions
-------------
id
channel_id
game_type
status
started_at
ended_at
```

---

## Game Participants

```
game_participants
-----------------
id
session_id
user_id
score
```

---

# Infrastructure Components

```
PostgreSQL
Redis
DigitalOcean Spaces (S3-compatible object storage)
coturn (STUN/TURN server)
```

Uses:

```
PostgreSQL      → relational data
Redis           → user presence and session caching (single instance)
Object storage  → voice messages and media files
coturn          → WebRTC NAT traversal (separate DigitalOcean Droplet)
```

---

# Deployment

```
Main Droplet (DigitalOcean)
 ├ Spring Boot backend
 ├ PostgreSQL
 └ Redis

TURN Droplet (DigitalOcean — small, separate)
 └ coturn (STUN/TURN)

Object Storage
 └ DigitalOcean Spaces
```

---

# Complete System Structure

```
frontend
 └ React + TypeScript (Feature-Sliced Design)
    Zustand · TanStack Query · TanStack Router
    STOMP over SockJS · shadcn/ui · React Hook Form

backend
 └ Spring Boot (Domain-Driven Design)
    Spring Security (OAuth2 Google + JWT)
    Spring WebSocket (STOMP)
    Flyway migrations

auth
 └ Google OAuth2 → JWT access token (15 min, in-memory on client)
    Refresh token → httpOnly cookie (30 days, hashed in DB)

database
 └ PostgreSQL

cache / presence
 └ Redis (single instance)

realtime
 └ WebSockets (STOMP over SockJS)

media
 └ WebRTC + coturn TURN server

storage
 └ DigitalOcean Spaces

deployment
 └ DigitalOcean Droplet — app + PostgreSQL + Redis
 └ DigitalOcean Droplet — coturn
```