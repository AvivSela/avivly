# Phase 1.3 — Flyway Baseline + Geo Migration

## Context

Spring Boot URL shortener at `backend/`. Database: PostgreSQL.
`application.yml` currently has `ddl-auto: update` (Hibernate manages schema).
This task introduces Flyway-managed migrations without yet switching `ddl-auto`
(that switch happens in Phase 5.2, after the baseline is verified).

**Prerequisite:** Phase 1.2 complete — `ClickAnalytics` must have the geo fields.

## Objective

Create two Flyway migration scripts:
- `V1__baseline.sql` — snapshot of the current schema (tables created by Hibernate `update`)
- `V2__geo_analytics.sql` — adds the geo columns and supporting indexes

## Implementation

### Step 1 — Create the migrations directory

```
backend/src/main/resources/db/migration/
```

### Step 2 — `V1__baseline.sql`

Generate this by inspecting the live database schema (or construct from the JPA entities).
The baseline must create `short_links` and `click_analytics` as they exist today (before geo fields):

```sql
-- Baseline schema snapshot (generated from existing Hibernate-managed DB)
CREATE TABLE IF NOT EXISTS short_links (
    id            BIGSERIAL PRIMARY KEY,
    short_code    VARCHAR(20)  NOT NULL UNIQUE,
    original_url  TEXT         NOT NULL,
    created_at    TIMESTAMP,
    expires_at    TIMESTAMP,
    max_clicks    INTEGER,
    total_clicks  INTEGER      NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    strategy_type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS click_analytics (
    id         BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(255) NOT NULL,
    clicked_at TIMESTAMP,
    referer    TEXT,
    user_agent TEXT,
    ip_address VARCHAR(45)
);
```

> **OQ-04:** If the live DB has additional columns or different types, adjust to match.
> Run `\d short_links` and `\d click_analytics` in psql to confirm exact DDL before committing.

### Step 3 — `V2__geo_analytics.sql`

```sql
ALTER TABLE click_analytics
    ADD COLUMN IF NOT EXISTS geo_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS country    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS city       VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_click_short_code
    ON click_analytics (short_code);

CREATE INDEX IF NOT EXISTS idx_click_short_code_country
    ON click_analytics (short_code, country);

CREATE INDEX IF NOT EXISTS idx_click_short_code_city
    ON click_analytics (short_code, city);
```

## Verify

Start the app against a **clean** (empty) PostgreSQL database and confirm Flyway applies
both migrations without error. Check the startup log for:
```
Successfully applied 2 migrations to schema "public"
```

Do NOT yet change `ddl-auto` — that is Phase 5.2.

## Commit

`feat: add Flyway V1 baseline and V2 geo_analytics migration (Phase 1.3)`
