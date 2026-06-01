# Agent Prompt: Frontend Dockerfile & nginx.conf (TASK-24)

## Project Context
You are building an **analytics-driven URL shortener**.
The frontend Vite + React project exists at `frontend/` with `npm run build` producing `frontend/dist/`.
The backend runs on port 8080 inside Docker under the service name `backend`.

## Your Task
Create the multi-stage Docker build file and nginx configuration for the frontend.

## Files to Create

### `frontend/Dockerfile`
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### `frontend/nginx.conf`
```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location /api/ {
        proxy_pass http://backend:8080/api/;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## Acceptance Criteria
- `frontend/Dockerfile` uses two stages: Node 18 Alpine build → nginx Alpine runtime
- `frontend/nginx.conf` proxies `/api/` to `http://backend:8080/api/` (backend is the Docker Compose service name)
- SPA routing is handled by `try_files $uri $uri/ /index.html`
- `package*.json` is copied before `npm install` to leverage Docker layer caching
- Port 80 is exposed
