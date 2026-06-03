# Phase 1.1 — Create `GeoStatus` Enum

## Context

Spring Boot URL shortener at `backend/`. Package: `com.memcyco.urlshortener`.
This is the first data-model task; subsequent tasks (1.2, 1.3) depend on it.

**Prerequisite:** Phase 0 complete (geoip2 dependency in pom.xml).

## Objective

Create the `GeoStatus` enum that records the outcome of a geo-resolution attempt for
each click event.

## Implementation

Create file: `backend/src/main/java/com/memcyco/urlshortener/model/GeoStatus.java`

```java
package com.memcyco.urlshortener.model;

public enum GeoStatus {
    PENDING,     // not yet resolved (default at insert time)
    RESOLVED,    // country/city successfully populated
    PRIVATE,     // RFC-1918 / loopback address — no lookup performed
    NOT_FOUND,   // public IP not present in the MaxMind DB
    ERROR        // lookup attempted but threw an unexpected exception
}
```

## Verify

```bash
cd backend && ./mvnw compile
```

Compilation must succeed with no errors.

## Commit

`feat: add GeoStatus enum (Phase 1.1)`
