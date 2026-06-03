# Phase 5.2 — Switch to Flyway-Managed Schema

## Context

Spring Boot URL shortener at `backend/`.
Config: `backend/src/main/resources/application.yml`
Currently: `spring.jpa.hibernate.ddl-auto: update`

**Prerequisites:**
- Phase 1.3 complete: `V1__baseline.sql` and `V2__geo_analytics.sql` exist in
  `backend/src/main/resources/db/migration/`
- The live database has already had the Flyway migrations applied (Phase 1.3 verified)

## Objective

Switch Hibernate to `validate` mode (schema must match entities; no auto-changes)
and configure Flyway to handle schema evolution from this point forward.

## Implementation

Edit `backend/src/main/resources/application.yml`.

Change the JPA block:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate      # was: update
```

Add a Flyway block under `spring:`:
```yaml
  flyway:
    baseline-on-migrate: true
    baseline-version: 1
```

The full `spring:` section should look like:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/urlshortener}
    username: ${SPRING_DATASOURCE_USERNAME:user}
    password: ${SPRING_DATASOURCE_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
  cache:
    type: caffeine
  flyway:
    baseline-on-migrate: true
    baseline-version: 1
```

## Verify

```bash
cd backend && ./mvnw spring-boot:run
```

Startup log must show:
- Flyway applying/validating migrations without error.
- No `SchemaManagementException` or `HibernateException` about schema mismatch.
- App starts and serves requests normally.

Also run the test suite — tests use H2, which is not affected by this change:
```bash
cd backend && ./mvnw test
```

## Commit

`feat: switch to Flyway-managed schema with ddl-auto validate (Phase 5.2)`
