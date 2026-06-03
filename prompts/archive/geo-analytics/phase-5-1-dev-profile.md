# Phase 5.1 — `application-dev.yml` for Zero-Setup Local Dev

## Context

Spring Boot URL shortener at `backend/`.
`backend/src/main/resources/application.yml` is the base config.
`backend/src/test/resources/GeoLite2-City-Test.mmdb` is the MaxMind Apache-2.0 test DB.

**Prerequisite:** Phase 0.2 complete — test fixture exists.

## Objective

Create a Spring profile `dev` that points `geo.db.path` at the test fixture so
developers can run the app locally with geo resolution working, without needing to
download the production MaxMind DB.

## Implementation

Create `backend/src/main/resources/application-dev.yml`:

```yaml
geo:
  db:
    path: src/test/resources/GeoLite2-City-Test.mmdb
```

That is the entire file. Spring Boot will merge it with `application.yml` when
the `dev` profile is active.

## Verify

Start the backend with the `dev` profile:

```bash
cd backend && SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Confirm in the startup log:
- No `geo.db.path is not set` warning.
- `GET /actuator/health` returns `geoResolver.status: "UP"`.

Issue a test redirect with a public IP:
```bash
curl -s -o /dev/null -w "%{http_code}" \
  -H "X-Real-IP: 81.2.69.142" \
  http://localhost:8080/{shortCode}
```

Query the DB — the inserted row should have `geo_status = 'RESOLVED'` and a non-null country.

## Commit

`feat: add application-dev.yml with geo test fixture for local dev (Phase 5.1)`
