# PRD: Error Page Redesign

## Overview

Replace Spring Boot's default Whitelabel Error Page and the minimal `LinkExpired` component with a consistent, branded set of error pages that follow the application's design language and give users a clear path forward.

---

## Problem Statement

The application currently exposes three degraded error surfaces:

1. **Spring Boot Whitelabel Error Page** — shown for any unhandled route or server error. Renders raw HTML with no branding, no actionable message, and no navigation.
2. **Blank frontend screen** — React Router has no wildcard route, so navigating to an unknown path renders nothing.
3. **Unstyled `LinkExpired` component** — a single line of grey text with no visual hierarchy, icon, or call to action.

All three break user trust and offer no recovery path.

---

## Goals

- Ensure every error state the user can encounter has a branded, informative, and actionable page.
- Keep the visual experience consistent across backend-served HTML and frontend React components.
- Give users a single, obvious CTA to return to the dashboard.

## Non-Goals

- Custom error pages for authenticated/authorization flows (future scope).
- Animated or illustrated error pages.
- Internationalisation (English only).

---

## User Stories

| ID | As a… | I want… | So that… |
|----|--------|---------|----------|
| US-1 | Visitor who mis-types a URL | To see a clear "page not found" message | I understand what happened and can navigate back |
| US-2 | User clicking a dead short link | To understand the link is no longer valid | I'm not confused thinking the app is broken |
| US-3 | Developer calling an unknown API endpoint | To receive a structured JSON error | I can handle it programmatically |
| US-4 | User on mobile | To have a readable, non-clipped error page | I can still navigate back without frustration |

---

## Scope

### In Scope

| Surface | Trigger |
|---------|---------|
| Backend HTML error page | Browser request to an unhandled backend route or a server error |
| Backend JSON error response | Request with `Accept: application/json` or path under `/api/` |
| Frontend `NotFound` component | React Router wildcard catch-all (`path="*"`) |
| Frontend `LinkExpired` component | Short code redirect to an expired, max-clicked, or deleted link |

### Out of Scope

- Changes to redirect logic or short-link validation.
- Changes to existing API error response contract (JSON shape must remain backward-compatible).
- Error pages for authentication/authorization states.

---

## Functional Requirements

### FR-1 — Backend Error Controller
- Implement Spring Boot's `ErrorController` interface mapped to `/error`.
- Detect request type: API/JSON vs browser navigation.
- **JSON path**: return `{ "status": <int>, "error": "<text>", "message": "<text>" }` with the correct HTTP status code.
- **HTML path**: return a self-contained HTML page with the correct HTTP status code.
- Human-readable messages for: `400`, `403`, `404`, `405`, `500`, `503`. Generic fallback for all others.

### FR-2 — Frontend `NotFound` Component
- New React component shown for any unrecognised frontend route.
- Add `path="*"` wildcard `<Route>` in `App.jsx`.
- Required elements: status code `404`, title "Page Not Found", one-sentence explanation, "Back to Dashboard" link to `/`.

### FR-3 — Frontend `LinkExpired` Component (redesign)
- Replace the single-line placeholder with a properly designed component.
- Required elements: eye-slash or broken-link icon, title "Link Unavailable", description of possible causes (expired / click limit / deleted), "Back to Dashboard" link to `/`.
- Existing route `/link-expired` and redirect behaviour are unchanged.

---

## UX & Design Requirements

- All error surfaces share the same visual language as the main application:
  - `bg-gray-50` full-height background
  - `bg-blue-600` header bar with "URL Shortener" label
  - Centred white card with `rounded-xl` corners and a subtle shadow
  - Blue-600 primary CTA button
- The backend HTML page uses **inline CSS only** — no external stylesheets, fonts, or scripts.
- All pages are **mobile-responsive** down to 375 px viewport width.
- WCAG AA colour contrast on all text/background pairings.
- No technical debug information (stack traces, request IDs) exposed to the user.

---

## Success Metrics

| Metric | Target |
|--------|--------|
| Whitelabel page impressions | 0 after release |
| Blank-screen on unknown frontend route | 0 after release |
| HTML error page weight | < 3 KB |
| WCAG AA contrast pass | 100% of text elements |

---

## Acceptance Criteria

| # | Scenario | Expected Result |
|---|----------|----------------|
| AC-1 | `GET /api/nonexistent` with `Accept: application/json` | `404` JSON `{ "status": 404, "error": "Not Found", "message": "The page you're looking for doesn't exist." }` |
| AC-2 | Browser navigates to `http://localhost:8080/doesnotexist` | Custom branded HTML page, HTTP 404, "Back to Dashboard" link |
| AC-3 | Browser navigates to frontend route `/anything-unknown` | `NotFound` React component, 404 messaging, "Back to Dashboard" link |
| AC-4 | Short code redirect hits expired or invalid link | Redesigned `LinkExpired` page with icon, description, and CTA |
| AC-5 | Any error page viewed at 375 px width | Card, status code, text, and CTA are all fully visible without horizontal scroll |
| AC-6 | Backend returns HTTP 500 | Custom HTML page with generic "unexpected error" message — no Whitelabel |
| AC-7 | Existing `GET /api/links` and `/api/r/{shortCode}` | Behaviour unchanged |
