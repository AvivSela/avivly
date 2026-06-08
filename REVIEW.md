# Code Review — authorization-feature branch

**Date:** 2026-06-08  
**Reviewer:** automated (claude-code /code-review)  
**Scope:** all changes on `authorization-feature` relative to `main`

---

## Summary

8 findings across the auth feature: 4 high-severity bugs that break correctness in common scenarios, 2 medium-severity issues that surface under standard production hardening, and 2 low-severity cleanups.

| # | Severity | File | Issue |
|---|----------|------|-------|
| 1 | High | `AuthService.java:24` | TOCTOU race → `DataIntegrityViolationException` → 500 instead of 409 |
| 2 | High | `application.yml:45` | `JWT_SECRET` not wired in `docker-compose.yml`/`.env.example` → startup crash |
| 3 | High | `api.js:14` | 401 interceptor redirects on `/login` — wrong-password error never shown |
| 4 | High | `SecurityConfig.java:118` | `WebMvcConfig` CORS wildcard is dead code; production CORS locked to `localhost:5173` |
| 5 | Medium | `LinkService.java:36` | `findAll()` not `@Transactional` → `LazyInitializationException` if OSIV disabled |
| 6 | Medium | `LinkController.java:28` | Unchecked `(Long)` cast → `ClassCastException` → 500 on future misconfiguration |
| 7 | Low | `JwtAuthenticationFilter.java:28` | JWT parsed twice per request (double HMAC verify) |
| 8 | Low | `LinkControllerIntegrationTest.java:56` | `registerAndGetToken`/`bearerHeaders` duplicated across two test classes |

---

## Findings

### 1. TOCTOU race on email uniqueness — `AuthService.java:24` · **High**

**What:** `existsByEmail()` check followed by `save()` is not atomic. Two concurrent registration requests for the same email both pass the check, both proceed to `save()`, and the second one hits the DB unique constraint. The resulting `DataIntegrityViolationException` is not handled by `GlobalExceptionHandler` (which only handles `MethodArgumentNotValidException` and `ResponseStatusException`), so Spring Boot returns HTTP 500.

**Fix:** Catch `DataIntegrityViolationException` in `AuthService.register()` and rethrow as `ResponseStatusException(CONFLICT)`, or add a handler in `GlobalExceptionHandler`. The `existsByEmail()` pre-check can remain as a fast-path but is no longer the sole guard.

```java
try {
    return userRepository.save(user);
} catch (DataIntegrityViolationException e) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
}
```

---

### 2. `JWT_SECRET` not wired in `docker-compose.yml` / `.env.example` — `application.yml:45` · **High**

**What:** `application.yml` references `${JWT_SECRET}` with no default value. Neither `docker-compose.yml`'s backend `environment` block nor `.env.example` defines this variable. Running `cp .env.example .env && make dev` crashes the backend at startup with `IllegalArgumentException: Could not resolve placeholder 'JWT_SECRET'`.

**Fix:** Add `JWT_SECRET` to both files.

`.env.example`:
```
JWT_SECRET=change-me-to-a-random-256-bit-secret
```

`docker-compose.yml` (backend service `environment`):
```yaml
- JWT_SECRET=${JWT_SECRET}
```

Also document in the README that this variable must be set before first run.

---

### 3. 401 interceptor redirects on `/login`, swallowing the error message — `api.js:14` · **High**

**What:** The axios response interceptor unconditionally calls `window.location.href = '/login'` on any 401 response. When a user submits wrong credentials, the server returns 401, the interceptor fires synchronously, and the page reloads before the `LoginForm` catch block (`setError('Invalid credentials')`) can execute. The user sees a blank page reload instead of an inline error.

**Fix:** Skip the redirect when the request was already targeting an auth endpoint.

```js
api.interceptors.response.use(
  res => res,
  err => {
    const url = err.config?.url ?? '';
    if (err.response?.status === 401 && !url.startsWith('/api/auth/')) {
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);
```

---

### 4. `WebMvcConfig` CORS wildcard is dead code — `SecurityConfig.java:118` · **High**

**What:** `SecurityConfig.corsConfigurationSource()` registers a Spring Security `CorsFilter` that runs before `DispatcherServlet`. It defaults `allowedOrigins` to `http://localhost:5173` when `CORS_ALLOWED_ORIGIN` is not set. Because the Security filter takes precedence, `WebMvcConfig.addCorsMappings()` (which uses `allowedOriginPatterns("*")`) is never reached for cross-origin requests. Any production frontend on a different origin gets a CORS rejection.

**Fix:** Remove the wildcard from `WebMvcConfig` (it is now dead code) and make `SecurityConfig` the single source of truth for CORS. Require `CORS_ALLOWED_ORIGIN` to be explicitly set in production, or provide a documented, secure default (e.g. empty = reject all cross-origin). Document this env var in `.env.example` and the README alongside `JWT_SECRET`.

---

### 5. `findAll()` not `@Transactional` → `LazyInitializationException` — `LinkService.java:36` · **Medium**

**What:** `LinkService.findAll()` lacks `@Transactional`. The Hibernate session closes as soon as the repository call returns. `LinkResponse::from` then accesses `link.getOwner().getId()` on an uninitialized lazy proxy outside a session. This throws `LazyInitializationException` when `spring.jpa.open-in-view=false` is set — the standard production hardening recommended by Spring.

**Fix:** Add `@Transactional(readOnly = true)` to `findAll()`. Alternatively, use a JPQL fetch join or a projection that avoids lazy loading altogether.

```java
@Transactional(readOnly = true)
public List<LinkResponse> findAll() { ... }
```

---

### 6. Unchecked `(Long)` cast on principal — `LinkController.java:28` · **Medium**

**What:** `(Long) auth.getPrincipal()` throws `ClassCastException` (→ HTTP 500) if any non-`Long` principal reaches the controller — e.g., an `AnonymousAuthenticationToken` whose principal is the `String` `"anonymousUser"`. This is a latent bug: today's `SecurityConfig` rules prevent it, but a future matcher change could expose it with no compile-time warning.

**Fix:** Use a safe cast with an explicit guard.

```java
if (!(auth.getPrincipal() instanceof Long userId)) {
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
}
```

---

### 7. JWT parsed twice per authenticated request — `JwtAuthenticationFilter.java:28` · **Low**

**What:** `validateToken(token)` and `getUserIdFromToken(token)` both call `parseClaims(token)`, which runs HMAC-SHA256 verification and base64+JSON parsing twice on every authenticated request. The redundant work is proportional to request volume.

**Fix:** Add a single `extractUserId(token)` method that validates and returns `Optional<Long>` in one parse, and replace the two-call pattern in the filter.

```java
private Optional<Long> extractUserId(String token) {
    try {
        Claims claims = parseClaims(token);
        return Optional.of(Long.parseLong(claims.getSubject()));
    } catch (JwtException | NumberFormatException e) {
        return Optional.empty();
    }
}
```

---

### 8. Auth test helpers duplicated across integration test classes — `LinkControllerIntegrationTest.java:56` · **Low**

**What:** `registerAndGetToken()` and `bearerHeaders()` are copied verbatim into both `LinkControllerIntegrationTest` and `LinkAuthorizationIntegrationTest`. If the auth registration path or `AuthResponse.token()` accessor changes, both copies must be updated independently. A missed update silently breaks one test class without a compile error.

**Fix:** Extract a shared `AuthTestSupport` base class or `@TestComponent` helper that both test classes extend or inject.

```java
// src/test/java/.../support/AuthTestSupport.java
public abstract class AuthTestSupport {
    protected String registerAndGetToken(MockMvc mvc, String email, String password) { ... }
    protected HttpHeaders bearerHeaders(String token) { ... }
}
```

---

## Recommended fix order

1. **#2** — unblock local development immediately (startup crash)  
2. **#3** — visible user-facing bug on the login page  
3. **#1** — data integrity / correct HTTP status  
4. **#4** — production CORS misconfiguration  
5. **#5, #6** — latent bugs surfaced by production hardening  
6. **#7, #8** — cleanups, can be batched with other refactors  
