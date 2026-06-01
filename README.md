# URL Shortener

A full-stack URL shortener with click analytics. Create short links, set expiry dates and click limits, and track usage with time-series charts. Built with Spring Boot, React, and PostgreSQL.

## Services

| Service | Image / Build | Port | Description |
|---------|--------------|------|-------------|
| `db` | `postgres:15-alpine` | `5432` | PostgreSQL database |
| `backend` | `./backend` | `8080` | Spring Boot REST API |
| `frontend` | `./frontend` | `3000` | React app served by nginx |

Startup order is enforced: `db` must pass its health check before `backend` starts, and `backend` must be up before `frontend`.

## Quick Start (Docker)

**Prerequisites:** Docker and Docker Compose installed.

```bash
# Build images and start all three services
docker-compose up --build

# Run in the background
docker-compose up --build -d
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Useful commands

```bash
# Stream logs for all services
docker-compose logs -f

# Stream logs for a single service
docker-compose logs -f backend

# Stop and remove containers (keeps the database volume)
docker-compose down

# Stop and wipe everything including the database volume
docker-compose down -v

# Rebuild a single service after a code change
docker-compose up --build backend
```

### Default credentials

| Variable | Value |
|----------|-------|
| Database name | `urlshortener` |
| Database user | `user` |
| Database password | `password` |

These are set in `docker-compose.yml` and injected into the backend at runtime. Change them there if needed — no other files need updating when running via Docker.

## Local Development (without Docker)

Use this when you want hot-reload for the backend or frontend without rebuilding images.

### Prerequisites

- Java 17+
- Maven 3.8+
- Node 18+
- PostgreSQL 15 running locally on port `5432`

### 1. Start PostgreSQL

Create the database and user to match the defaults:

```sql
CREATE USER "user" WITH PASSWORD 'password';
CREATE DATABASE urlshortener OWNER "user";
```

Or run a one-off Postgres container:

```bash
docker-compose up -d db
```

### 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`. It connects to `localhost:5432/urlshortener` by default (configured in `backend/src/main/resources/application.yml`). Override any value with environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/urlshortener \
SPRING_DATASOURCE_USERNAME=user \
SPRING_DATASOURCE_PASSWORD=password \
mvn spring-boot:run
```

Hibernate auto-creates and migrates the schema on startup (`ddl-auto: update`).

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server starts on `http://localhost:5173`. All `/api/*` requests are proxied to `http://localhost:8080` via Vite's proxy (configured in `vite.config.js`), so no CORS setup is needed.

## API Reference

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| POST | `/api/links` | Create a short link | 201 Created |
| GET | `/api/links` | List all short links | 200 OK |
| PUT | `/api/links/{id}` | Update a short link | 200 OK |
| DELETE | `/api/links/{id}` | Delete a short link | 204 No Content |
| GET | `/api/links/{shortCode}/analytics` | Get click analytics | 200 OK |
| GET | `/{shortCode}` | Redirect to original URL | 302 Found / 410 Gone |

## Architecture Notes

Short links are cached in memory using Caffeine (`@Cacheable`) with a 10-minute TTL, so repeated redirects are served without hitting the database. Click analytics are recorded asynchronously via Spring's `@Async` with a dedicated thread pool, ensuring redirects are never delayed by database writes. Every redirect validates the link via `ShortLink.isValid()`, enforcing expiry dates, max-click limits, and the active flag before serving or rejecting the request.
