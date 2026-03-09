# LLM-Optimized Development Plan

**Dissension — Discord-Like Platform**

Frontend: React + TypeScript (Feature-Sliced Design)
State: Zustand · TanStack Query · TanStack Router
UI: shadcn/ui · React Hook Form
WebSocket: STOMP over SockJS

Backend: Spring Boot (Domain-Driven Design)
Security: Spring Security (OAuth2 Google + JWT with httpOnly refresh token cookie)
WebSocket: Spring WebSocket (STOMP broker)
Migrations: Flyway

Deployment: DigitalOcean Droplet (single instance) + coturn TURN server (separate Droplet)

Game development and game LLM generation are **separate systems**.

The platform must provide **embedding infrastructure only**.

Games are built with **LibGDX** compiled to HTML5/WebGL via the GWT backend, embedded via canvas/WebGL with a **postMessage event bridge**.

Structured prompts use defined sections such as role, context, task, constraints, and output format to improve reliability when interacting with large language models. ([arXiv](https://arxiv.org/abs/2601.04055?utm_source=chatgpt.com))

Frontend architecture follows **Feature-Sliced Design layers**:

```
app
pages
widgets
features
entities
shared
```

These layers organize frontend code around business domains and interactions. ([FSD](https://fsd.how/docs/get-started/overview?utm_source=chatgpt.com))

---

# Phase 1

# System Foundation

```
ROLE
Software architecture planner.

CONTEXT
System architecture:

Frontend
React + TypeScript
Feature-Sliced Design
Zustand (state) · TanStack Query (data fetching) · TanStack Router (routing)
shadcn/ui (components) · React Hook Form (forms) · STOMP over SockJS (WebSocket)

Backend
Spring Boot
Domain-Driven Design
Spring Security (OAuth2 Google + JWT) · Spring WebSocket (STOMP) · Flyway

Infrastructure
PostgreSQL
Redis (presence and session caching — single instance)
DigitalOcean Spaces (object storage)
WebSockets (STOMP over SockJS)
WebRTC + coturn TURN server
DigitalOcean Droplets (app on main Droplet, coturn on separate Droplet)

Application domain:

users
servers
channels
messages
conversations (direct messages — DIRECT and GROUP)
calls
friends
presence
games

Games are external modules.
The main platform embeds them.

OBJECTIVE
Create the system foundation and repository layout.

CONSTRAINTS
Single backend service.
Single instance deployment (DigitalOcean Droplet).
Frontend follows Feature-Sliced Design.
Backend follows Domain-Driven Design.
Game systems remain external.

TASKS

1 Define monorepo structure.

2 Define folder structure for frontend.

3 Define folder structure for backend.

4 Define infrastructure folder.

5 Define environment configuration.

6 Define authentication configuration (OAuth2 Google, JWT, refresh token strategy).

OUTPUT FORMAT

RepositoryStructure
FrontendStructure
BackendStructure
InfrastructureStructure
EnvironmentConfiguration
```

---

# Phase 2

# Database Architecture

```
ROLE
Database architect.

CONTEXT
System entities:

User (auth_provider: GOOGLE | LOCAL, password_hash nullable for OAuth-only accounts)
RefreshToken (token_hash, expires_at, revoked — never store raw token)
Friend (status: PENDING, ACCEPTED, BLOCKED)
Conversation (type: DIRECT, GROUP — for direct messages)
ConversationParticipant (conversation_id, user_id, joined_at)
Server
ServerMember (role: OWNER, MOD, MEMBER)
Channel (belongs to server only — DMs use Conversation)
Message (channel_id nullable, conversation_id nullable — exactly one must be set, deleted_at for soft delete)
Reaction
Call
CallParticipant
VoiceMessage
GameSession
GameParticipant

OBJECTIVE
Create a normalized relational schema.

CONSTRAINTS
Use PostgreSQL.
Use UUID primary keys.
Use foreign keys.
Add indexes for high-frequency queries.
Use Flyway for schema migrations.

TASKS

1 Define tables.

2 Define relationships.

3 Define indexes.

4 Generate SQL schema.

OUTPUT FORMAT

Tables
Relationships
Indexes
SQLSchema
```

---

# Phase 3

# Backend Domain Model

```
ROLE
Domain-Driven Design architect.

CONTEXT
Backend architecture follows DDD.

Domain modules:

user
auth (OAuth2 flow, JWT issuance, refresh token lifecycle)
server
channel
message
conversation
call
friend
game_session

OBJECTIVE
Create domain layer structure.

CONSTRAINTS
Domain layer contains only business logic.
No infrastructure logic.

TASKS

1 Define aggregates.

2 Define entities.

3 Define value objects.

4 Define repositories.

5 Define domain services.

OUTPUT FORMAT

DomainPackages
Aggregates
Entities
ValueObjects
Repositories
DomainServices
```

---

# Phase 4

# Application Layer

```
ROLE
Backend application architect.

CONTEXT
Application layer coordinates domain logic.

OBJECTIVE
Define use cases and application services.

CONSTRAINTS
Application layer does not contain persistence code.

TASKS

1 Define use cases.

2 Define DTOs.

3 Define application services.

OUTPUT FORMAT

UseCases
DTOs
ApplicationServices
```

---

# Phase 5

# API Interface

```
ROLE
API design architect.

CONTEXT
Backend exposes REST APIs and WebSocket endpoints.

Auth strategy:
Google OAuth2 via Spring Security OAuth2 client.
JWT access token (15 min) returned in response body.
Refresh token (30 days) set as httpOnly cookie.
WebSocket connections authenticated via JWT in STOMP CONNECT headers.

OBJECTIVE
Define all external interfaces.

CONSTRAINTS
REST for request/response.
WebSockets (STOMP over SockJS) for realtime events.

TASKS

1 Define REST endpoints.

2 Define authentication endpoints (OAuth2 redirect, callback, token refresh, logout).

3 Define WebSocket STOMP topics and destinations.

4 Define request and response models.

OUTPUT FORMAT

RESTEndpoints
AuthEndpoints
WebSocketChannels
RequestModels
ResponseModels
```

---

# Phase 6

# Realtime Messaging System

```
ROLE
Realtime system designer.

CONTEXT
Realtime functionality includes:

chat
presence
typing indicators
message updates

OBJECTIVE
Design WebSocket messaging architecture.

CONSTRAINTS
Messages persist in database.
Presence stored in Redis (single instance — no pub/sub broadcast needed at scale).
STOMP broker handles in-process message routing.
Typing indicators expire server-side after 5 seconds.

TASKS

1 Define event types.

2 Define message flow.

3 Define presence system.

4 Define state synchronization.

OUTPUT FORMAT

EventTypes
MessageFlow
PresenceArchitecture
StateSynchronization
```

---

# Phase 7

# Call System

```
ROLE
WebRTC architecture planner.

CONTEXT
Calls include:

voice calls
video calls
screen sharing

OBJECTIVE
Design signaling and session management.

CONSTRAINTS
WebRTC handles media transport.
Backend handles signaling via STOMP WebSocket.
coturn TURN server handles NAT traversal (deployed on a separate DigitalOcean Droplet).
Backend issues STUN/TURN credentials to clients at call initiation.

TASKS

1 Define signaling events.

2 Define call session lifecycle.

3 Define participant state.

OUTPUT FORMAT

SignalingEvents
CallLifecycle
ParticipantState
```

---

# Phase 8

# Frontend Feature-Sliced Design

```
ROLE
React architecture planner.

CONTEXT
Frontend uses Feature-Sliced Design.

Tech stack:
Zustand — global client state (auth, UI, presence)
TanStack Query — server state, caching, background refetch
TanStack Router — type-safe file-based routing
STOMP over SockJS — WebSocket client protocol
shadcn/ui — headless Tailwind-based component library
React Hook Form — form state and validation

Layers:

app
pages
widgets
features
entities
shared

OBJECTIVE
Define frontend architecture.

CONSTRAINTS
Follow layer import rules.

TASKS

1 Define page slices.

2 Define widget components.

3 Define feature modules.

4 Define entity models.

OUTPUT FORMAT

LayerStructure
PageSlices
Widgets
Features
Entities
```

---

# Phase 9

# Chat Feature Implementation

```
ROLE
Realtime frontend engineer.

CONTEXT
Chat functionality includes:

message sending
message editing
reactions
typing indicators

OBJECTIVE
Design chat implementation.

CONSTRAINTS
Messages synchronized through WebSocket.

TASKS

1 Define chat state.

2 Define UI components.

3 Define event handling.

OUTPUT FORMAT

ChatStateModel
ChatComponents
EventHandling
```

---

# Phase 10

# External Game Platform Integration

```
ROLE
Game platform integration architect.

CONTEXT
Games are external modules.

Game development is separate.

Platform responsibilities:

game embedding
session management
player synchronization

OBJECTIVE
Define game integration interface.

CONSTRAINTS
Game engine not implemented in backend.
Platform provides embedding conditions.
Games are built with LibGDX compiled to HTML5/WebGL via the GWT backend.
Embedding uses canvas/WebGL direct DOM mounting — no iframe.
postMessage API is the sole event bridge between the LibGDX JS bundle and React.
Message schema must be versioned to allow game updates without breaking the platform.

TASKS

1 Define game session REST API (create, join, end session).

2 Define game session domain model.

3 Define player synchronization WebSocket events.

4 Define canvas mount/unmount lifecycle contract.

5 Define postMessage event schema (platform → game and game → platform).

OUTPUT FORMAT

GameSessionAPI
GameSessionModel
PlayerSyncEvents
CanvasLifecycle
PostMessageSchema
```

---

# Phase 11

# Frontend Game Embedding Layer

```
ROLE
Frontend system architect.

CONTEXT
Games run as external modules.

Frontend must embed them inside the platform.

OBJECTIVE
Define frontend game embedding architecture.

CONSTRAINTS
LibGDX GWT output mounts into a canvas element owned by React.
React controls the canvas container lifecycle (mount on session start, unmount on session end).
postMessage is the only communication channel between the LibGDX JS bundle and React.
Unknown or malformed message types are silently discarded.

TASKS

1 Define GameWidget component (canvas container, mount/unmount lifecycle, game bundle loading).

2 Define GameSession UI (score overlay, HUD, pause controls rendered by React outside the canvas).

3 Define postMessage event bridge (outbound schema, inbound handler, message validation).

OUTPUT FORMAT

GameWidgetComponent
GameSessionUI
PostMessageBridgeSchema
InboundEventHandlers
```

---

# Phase 12

# System Integration

```
ROLE
Integration engineer.

CONTEXT
Frontend and backend systems must connect.

OBJECTIVE
Integrate APIs, WebSockets, and WebRTC.

CONSTRAINTS
All state synchronization must be consistent.

TASKS

1 Integrate REST APIs.

2 Integrate WebSocket events.

3 Integrate call signaling.

4 Integrate game embedding APIs.

OUTPUT FORMAT

APIIntegration
RealtimeIntegration
GameIntegration
```

---

# Phase 13

# Deployment Architecture

```
ROLE
DevOps architect.

CONTEXT
Production deployment on DigitalOcean.
Target scale: 250–450 concurrent users.
Single-instance deployment — no horizontal scaling required.

Infrastructure:
Droplet 1 (main) — Spring Boot backend, PostgreSQL, Redis
Droplet 2 (TURN) — coturn STUN/TURN server (small, separate)
DigitalOcean Spaces — S3-compatible object storage

OBJECTIVE
Define infrastructure and deployment architecture for DigitalOcean single-instance setup.

CONSTRAINTS
Use Docker and Docker Compose on DigitalOcean Droplets.
Single backend instance — no load balancer or Redis pub/sub broadcast needed.
coturn runs on a separate small Droplet to isolate media traffic.
Secrets managed via environment variables (not committed to repository).

TASKS

1 Define Docker Compose configuration for main Droplet (backend, PostgreSQL, Redis).

2 Define coturn configuration for TURN Droplet (credentials, firewall rules).

3 Define environment variable structure (.env layout, secret naming conventions).

4 Define CI/CD pipeline (GitHub Actions → SSH deploy to main Droplet on push to main).

OUTPUT FORMAT

DockerComposeConfig
CoturnConfig
EnvironmentConfig
CICDPipeline
```

---

# Game Development Separation

```
Game systems exist outside the main repository.

Game repository responsibilities:

game logic
game rendering
game AI
game LLM generation

Platform responsibilities:

game session API
player synchronization
embedding environment
state events
```

---

# Final Development Flow

```
Phase1  System foundation
Phase2  Database schema
Phase3  Domain model
Phase4  Application layer
Phase5  API interface
Phase6  Realtime messaging
Phase7  Call system
Phase8  Frontend architecture
Phase9  Chat feature
Phase10 Game integration
Phase11 Frontend game embedding
Phase12 System integration
Phase13 Deployment
```