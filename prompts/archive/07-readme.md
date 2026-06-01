# Agent Prompt: README.md (TASK-27)

## Project Context
You have just finished building an **analytics-driven URL shortener** with:
- **Backend:** Spring Boot 3, Java 17, PostgreSQL, Caffeine cache, async analytics
- **Frontend:** React + Vite + Tailwind CSS + Recharts
- **Deployment:** Docker Compose with 3 services (db, backend, frontend)

## Architecture Summary
- Short links are cached in Caffeine (`@Cacheable`) with a 10-minute TTL for fast redirects
- Click analytics are recorded asynchronously (`@Async` with a dedicated thread pool) so redirects are never blocked by DB writes
- `ShortLink.isValid()` enforces expiry, max-click limits, and active flag — checked on every redirect
- Frontend proxies all `/api/` requests through nginx to the backend in production, and through Vite's dev proxy in development

## API Endpoints
| Method | Path | Description | Response |
|--------|------|-------------|----------|
| POST | `/api/links` | Create a short link | 201 Created |
| GET | `/api/links` | List all short links | 200 OK |
| PUT | `/api/links/{id}` | Update a short link | 200 OK |
| DELETE | `/api/links/{id}` | Delete a short link | 204 No Content |
| GET | `/api/links/{shortCode}/analytics` | Get click analytics | 200 OK |
| GET | `/{shortCode}` | Redirect to original URL | 302 Found / 410 Gone |

## Your Task
Create `README.md` at the project root.

The README must include:
1. **Overview** — one paragraph describing what the app does
2. **Quick Start** — `docker-compose up --build`, then open `http://localhost:3000`
3. **API Reference** — the table of endpoints above
4. **Local Development** — how to run backend with `mvn spring-boot:run` and frontend with `npm run dev` separately (without Docker), including prerequisites (Java 17, Node 18, PostgreSQL running locally)
5. **Architecture Notes** — 2-3 sentences on caching (`@Cacheable`) and async analytics (`@Async`)

## Acceptance Criteria
- `README.md` exists at the project root
- Someone unfamiliar with the project can get it running from the README alone (prerequisites listed, commands copy-pasteable)
- All 6 API endpoints are documented in a table
- Quick Start section has a single command: `docker-compose up --build`
- Local Development section mentions both `mvn spring-boot:run` and `npm run dev`
