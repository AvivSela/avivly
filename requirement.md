# Analytics-Driven URL Shortener — Interview POC Specification

**Time Budget:** 3 hours | **Stack:** Spring Boot 3 + React + PostgreSQL + Docker

---

## Project Structure

```
/
├── backend/          # Spring Boot 3 (Java 17)
├── frontend/         # React + Vite + Tailwind
├── docker-compose.yml
└── README.md
```

---

## 1. Database Schema

```sql
CREATE TABLE short_links (
    id           BIGSERIAL PRIMARY KEY,
    short_code   VARCHAR(50) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    strategy     VARCHAR(30) DEFAULT 'RANDOM_BASE62',
    is_active    BOOLEAN DEFAULT TRUE,
    max_clicks   INT DEFAULT NULL,          -- NULL = unlimited
    total_clicks INT DEFAULT 0,
    expires_at   TIMESTAMP DEFAULT NULL,   -- NULL = never
    tags         VARCHAR(255),             -- comma-separated
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE click_analytics (
    id          BIGSERIAL PRIMARY KEY,
    short_code  VARCHAR(50) NOT NULL REFERENCES short_links(short_code) ON DELETE CASCADE,
    clicked_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    referer     TEXT,
    user_agent  TEXT,
    ip_address  VARCHAR(45)
);

CREATE INDEX idx_short_code       ON short_links(short_code);
CREATE INDEX idx_analytics_code   ON click_analytics(short_code);
CREATE INDEX idx_analytics_time   ON click_analytics(short_code, clicked_at);
```

---

## 2. Spring Boot Architecture (Core Evaluation Criteria)

### 2.1 Dependencies (`pom.xml`)

```xml
<dependencies>
    <dependency><!-- Spring Web --></dependency>
    <dependency><!-- Spring Data JPA --></dependency>
    <dependency><!-- PostgreSQL Driver --></dependency>
    <dependency><!-- Spring Cache + Caffeine --></dependency>
    <dependency><!-- Spring Validation --></dependency>
</dependencies>
```

Key: `spring-boot-starter-cache` + `com.github.ben-manes.caffeine:caffeine`

### 2.2 Application Class

```java
@SpringBootApplication
@EnableAsync          // REQUIRED — enables @Async thread pool
@EnableCaching        // REQUIRED — enables @Cacheable
public class UrlShortenerApplication { ... }
```

### 2.3 Cache Configuration

```java
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCache linksCache = new CaffeineCache("shortLinks",
            Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build());
        return new SimpleCacheManager(List.of(linksCache));
    }
}
```

### 2.4 Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("analytics-");
        executor.initialize();
        return executor;
    }
}
```

---

## 3. API Contract

### 3.1 Public Redirect (Critical Path — must be fastest)

| Method | Path | Response |
|--------|------|----------|
| `GET` | `/{shortCode}` | `302 Found` → original URL |

**Redirect logic (implement in this exact order):**
1. `linkService.findByShortCode(shortCode)` — hits Caffeine cache, not DB
2. Validate: active + not expired + clicks not exhausted → else `410 Gone`
3. Fire-and-forget: `analyticsService.logClickAsync(...)` — never blocks redirect
4. Return `302` with `Location` header

```java
@GetMapping("/{shortCode}")
public ResponseEntity<Void> redirect(@PathVariable String shortCode,
                                      HttpServletRequest request) {
    ShortLink link = linkService.findByShortCode(shortCode);
    if (link == null || !link.isValid()) {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
    analyticsService.logClickAsync(
        shortCode,
        request.getHeader("Referer"),
        request.getHeader("User-Agent"),
        request.getRemoteAddr()
    );
    return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(link.getOriginalUrl()))
            .build();
}
```

### 3.2 Admin CRUD (`/api/links`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/links` | Create short link |
| `GET` | `/api/links` | List all links |
| `PUT` | `/api/links/{id}` | Update link |
| `DELETE` | `/api/links/{id}` | Delete link (cascades analytics) |
| `GET` | `/api/links/{shortCode}/analytics` | Analytics for one link |

**POST payload:**
```json
{
  "originalUrl": "https://example.com/very/long/path",
  "customAlias": "my-promo",
  "strategy": "RANDOM_BASE62",
  "maxClicks": 1000,
  "expiresAt": "2026-12-31T23:59:59Z",
  "tags": "promo,marketing"
}
```

**Analytics response:**
```json
{
  "totalClicks": 42,
  "clicksOverTime": [
    { "date": "2026-06-01", "count": 10 }
  ],
  "topReferrers": [
    { "referer": "https://twitter.com", "count": 25 }
  ],
  "topUserAgents": [
    { "userAgent": "Chrome/125", "count": 30 }
  ]
}
```

---

## 4. Service Layer

### LinkService

```java
@Service
public class LinkService {

    @Cacheable(value = "shortLinks", key = "#shortCode")
    public ShortLink findByShortCode(String shortCode) {
        return repo.findByShortCode(shortCode).orElse(null);
    }

    @CacheEvict(value = "shortLinks", key = "#shortCode")
    public void evict(String shortCode) {}

    public ShortLink create(CreateLinkRequest req) {
        String code = req.getCustomAlias() != null
            ? req.getCustomAlias()
            : Base62.generate(7);          // see §5
        // persist and return
    }
}
```

### AnalyticsService

```java
@Service
public class AnalyticsService {

    @Async   // runs on analytics thread pool, never blocks caller
    public void logClickAsync(String shortCode, String referer,
                              String userAgent, String ip) {
        clickRepo.save(new ClickAnalytics(shortCode, referer, userAgent, ip));
        linkRepo.incrementClicks(shortCode);  // @Modifying @Query
    }
}
```

---

## 5. Base62 Code Generation

```java
public class Base62 {
    private static final String CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int length) {
        return IntStream.range(0, length)
            .mapToObj(i -> String.valueOf(CHARS.charAt(RANDOM.nextInt(62))))
            .collect(Collectors.joining());
    }
}
```

---

## 6. Entity: `ShortLink`

```java
@Entity @Table(name = "short_links")
public class ShortLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String shortCode;
    private String originalUrl;
    private String strategy;
    private boolean isActive;
    private Integer maxClicks;
    private int totalClicks;
    private LocalDateTime expiresAt;
    private String tags;
    private LocalDateTime createdAt;

    public boolean isValid() {
        if (!isActive) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        if (maxClicks != null && totalClicks >= maxClicks) return false;
        return true;
    }
}
```

---

## 7. Frontend (React + Vite + Tailwind)

Single-page dashboard — no routing needed.

```
┌─ Header: "URL Shortener" ──────────────────────────────────────────┐
│                                                                     │
│  ┌─ Create / Edit Form ──────┐  ┌─ Links Table ───────────────┐    │
│  │  Long URL *               │  │  Short Link │ URL │ Clicks   │    │
│  │  Custom Alias (optional)  │  │  Status     │ Created │ Acts │    │
│  │  Strategy [dropdown]      │  │  [Edit] [Delete] [Stats]     │    │
│  │  Expires At (optional)    │  └──────────────────────────────┘    │
│  │  Max Clicks (optional)    │                                      │
│  │  Tags                     │                                      │
│  │  [Create Link]            │                                      │
│  └───────────────────────────┘                                      │
│                                                                     │
│  ┌─ Analytics Panel (shown when "Stats" clicked) ─────────────┐    │
│  │  Total Clicks: 42                                           │    │
│  │  [Line Chart: clicks per day — use Recharts]               │    │
│  │  Top Referrers    │   Top User Agents                       │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

**Key components:**
- `LinkForm.jsx` — create/edit form, controlled inputs, axios POST/PUT
- `LinksTable.jsx` — list all links, action buttons
- `AnalyticsPanel.jsx` — stats on row click, Recharts `<LineChart>`
- `api.js` — axios instance pointing to `http://localhost:8080`

---

## 8. Docker Deployment

### `backend/Dockerfile`

```dockerfile
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

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

### `frontend/nginx.conf` (needed for SPA + API proxy)

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location /api/ {
        proxy_pass http://backend:8080;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

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

---

## 9. Application Properties (`backend/src/main/resources/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/urlshortener
    username: user
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  cache:
    type: caffeine

server:
  port: 8080

logging:
  level:
    root: WARN
    com.yourpackage: INFO
```

---

## 10. 3-Hour Execution Plan

### Hour 1 — Backend Core (0:00–1:00)
- [ ] `spring initializr` → Web, JPA, PostgreSQL, Cache, Validation
- [ ] Create `ShortLink` + `ClickAnalytics` entities
- [ ] Write `ShortLinkRepository` + `incrementClicks` `@Modifying` query
- [ ] Implement `Base62.generate()`
- [ ] Implement `LinkService` with `@Cacheable`
- [ ] Implement `AnalyticsService` with `@Async`
- [ ] Wire `RedirectController` (`GET /{shortCode}`)
- [ ] Wire `LinkController` (full CRUD + analytics endpoint)
- [ ] Add `@EnableAsync` + `@EnableCaching` to main class
- [ ] Smoke-test with `curl` against local Postgres

### Hour 2 — Frontend (1:00–2:00)
- [ ] `npm create vite@latest frontend -- --template react`
- [ ] Install: `axios`, `recharts`, `tailwindcss`
- [ ] Build `api.js` (axios base URL)
- [ ] Build `LinkForm` (create/edit, strategy dropdown)
- [ ] Build `LinksTable` (list, delete, trigger analytics panel)
- [ ] Build `AnalyticsPanel` (Recharts line chart + referrers/agents lists)
- [ ] Wire CORS in Spring (`@CrossOrigin` or `WebMvcConfigurer`)

### Hour 3 — Integration & Polish (2:00–3:00)
- [ ] Write 2–3 integration tests (redirect flow, expired link, exhausted clicks)
- [ ] Write `Dockerfiles` for backend and frontend
- [ ] Write `docker-compose.yml` with `healthcheck` on db
- [ ] Write `nginx.conf`
- [ ] `docker-compose up --build` — verify everything starts
- [ ] Test full golden path end-to-end in browser
- [ ] Write `README.md` (setup instructions + `docker-compose up`)

---

## 11. Key Interview Talking Points

| Topic | What to Say |
|-------|-------------|
| **Caching** | Caffeine in-process cache avoids DB round-trip on every redirect; 10-min TTL, 10K entry cap. Evict on update/delete. |
| **Async** | `@Async` on analytics write means redirect returns in <5ms regardless of DB write latency. Uses dedicated thread pool. |
| **Scalability** | Stateless backend; cache could be replaced with Redis for multi-instance. Analytics writes are append-only and could be batched. |
| **Correctness** | `isValid()` checks active flag, expiry, and click cap atomically before redirect. `total_clicks` incremented via `@Modifying` query. |
| **Docker** | DB healthcheck prevents backend race condition at startup. Nginx proxies `/api/` to avoid CORS in production. |
