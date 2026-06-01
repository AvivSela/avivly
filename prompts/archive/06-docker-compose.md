# Agent Prompt: docker-compose.yml (TASK-25)

## Project Context
You are building an **analytics-driven URL shortener**.
The following Dockerfiles already exist:
- `backend/Dockerfile` — builds Spring Boot app, exposes port 8080
- `frontend/Dockerfile` — builds React app with nginx, exposes port 80; nginx proxies `/api/` to `http://backend:8080/api/`

The backend reads DB connection from environment variables:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

## Your Task
Create the Docker Compose orchestration file at the project root.

## File to Create

### `docker-compose.yml`
```yaml
version: '3.8'
services:
  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: urlshortener
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user -d urlshortener"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/urlshortener
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_JPA_HIBERNATE_DDL_AUTO: update

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
```

## Acceptance Criteria
- File is at `docker-compose.yml` (project root, not inside `backend/` or `frontend/`)
- `db` service has a `healthcheck` using `pg_isready`
- `backend` service has `depends_on: db: condition: service_healthy` — it waits for DB to be healthy before starting
- `frontend` service maps container port 80 to host port 3000
- `docker-compose config` validates without error (run this to verify)
