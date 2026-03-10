# Dissension — Local Development Setup Guide

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java JDK | 21+ | Backend runtime |
| Node.js | 22+ | Frontend tooling |
| Docker Desktop | latest | PostgreSQL + Redis via Docker Compose |
| Git | any | Source control |

Optional but recommended:
- **IntelliJ IDEA** (Community or Ultimate) for backend
- **VS Code** for frontend

---

## 1. Clone the repository

```bash
git clone <repo-url>
cd dissention-app
```

---

## 2. Start infrastructure with Docker

Create `docker-compose.yml` in the project root:

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: dissension
      POSTGRES_USER: dissension
      POSTGRES_PASSWORD: dissension
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

Then start it:

```bash
docker compose up -d
```

Verify both containers are running:

```bash
docker compose ps
```

---

## 3. Google OAuth2 setup

You need a Google OAuth2 client for login to work.

1. Go to [Google Cloud Console → APIs & Services → Credentials](https://console.cloud.google.com/apis/credentials)
2. Click **Create Credentials → OAuth client ID**
3. Application type: **Web application**
4. Add authorized redirect URI:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
5. Copy the **Client ID** and **Client Secret**

---

## 4. Configure backend environment

```bash
cd backend
cp .env.example .env
```

Edit `.env` and fill in at minimum:

```env
# These have working defaults for local dev — change for production:
DB_URL=jdbc:postgresql://localhost:5432/dissension
DB_USERNAME=dissension
DB_PASSWORD=dissension
REDIS_HOST=localhost
REDIS_PORT=6379

# Generate a secure random string (at least 32 characters):
# Linux/macOS: openssl rand -base64 64
# PowerShell:  [Convert]::ToBase64String((1..64 | ForEach-Object { [byte](Get-Random -Max 256) }))
JWT_SECRET=your-random-secret-here

# From Google Cloud Console:
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret

# Object storage — skip for now, required when implementing file upload (Phase 8)
# For local dev you can use MinIO: https://min.io/docs/minio/container/index.html
SPACES_ACCESS_KEY=placeholder
SPACES_SECRET_KEY=placeholder
SPACES_BUCKET=dissension
SPACES_ENDPOINT=http://localhost:9000
SPACES_REGION=us-east-1

# TURN server — skip for now, required for production WebRTC (Phase 10)
# For local dev on the same machine, STUN-only works fine
TURN_HOST=placeholder
TURN_USERNAME=placeholder
TURN_CREDENTIAL=placeholder
```

> **Note:** The backend reads `.env` only if you pass it explicitly (see the run command below) or use IntelliJ's `.env file` run configuration. Spring Boot does not auto-load `.env` files — the variables must be present in the shell or passed as `-D` flags.

---

## 5. Run the backend

### Option A — Pass env vars directly (PowerShell)

```powershell
cd backend
$env:JWT_SECRET="your-secret"
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
.\mvnw.cmd spring-boot:run
```

### Option B — Use a `.env` loader (recommended)

Install [`dotenv-run`](https://github.com/nicolo-ribaudo/dotenv-run) or use IntelliJ's built-in `.env` file support in Run Configurations.

Alternatively, export all variables from the `.env` file in PowerShell:

```powershell
Get-Content backend\.env | Where-Object { $_ -notmatch '^#' -and $_ -match '=' } | ForEach-Object {
  $name, $value = $_ -split '=', 2
  [System.Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
}
cd backend
.\mvnw.cmd spring-boot:run
```

The backend starts on **http://localhost:8080**.  
Flyway will automatically apply `V1__init_schema.sql` on first startup.

---

## 6. Run the frontend

```bash
cd frontend
cp .env.example .env.local
npm install        # already done if you followed prior setup
npm run dev
```

The frontend starts on **http://localhost:5173**.

---

## 7. Verify everything works

| Check | URL |
|-------|-----|
| Backend health | http://localhost:8080/actuator/health |
| Frontend | http://localhost:5173 |
| Google OAuth login | http://localhost:8080/oauth2/authorization/google |

---

## Services summary

| Service | Local default | Required for |
|---------|--------------|--------------|
| PostgreSQL | `localhost:5432` | All backend functionality |
| Redis | `localhost:6379` | Presence, session caching |
| Google OAuth | Cloud Console | Authentication |
| DigitalOcean Spaces / MinIO | `localhost:9000` (MinIO) | File/media uploads (Phase 8+) |
| coturn TURN server | — | Production WebRTC (Phase 10+) |

---

## API keys / external services reference

| Service | Where to get it | Needed from |
|---------|----------------|-------------|
| Google OAuth2 Client ID & Secret | [console.cloud.google.com](https://console.cloud.google.com/apis/credentials) | Phase 4 (auth) |
| DigitalOcean Spaces key & secret | [cloud.digitalocean.com/spaces](https://cloud.digitalocean.com/spaces) | Phase 8 (file storage) |
| coturn credentials | Self-hosted on a DigitalOcean Droplet | Phase 10 (WebRTC calls) |

---

## Production deployment notes

- All `.env` variables become environment variables on the DigitalOcean Droplet (set via the control panel or `export` in systemd unit files).
- `JWT_SECRET` must be a cryptographically random string — **never reuse the dev value**.
- Set `GOOGLE_CLIENT_ID` redirect URI to your production domain in the Google Cloud Console.
- coturn runs on a separate DigitalOcean Droplet; open UDP ports 3478 and 49152–65535 in the firewall.
