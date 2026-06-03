# PR Review Fix Checklist — geo-feature branch

Apply in this order to avoid compile errors mid-way. Each step must compile clean before the next begins.

## Sequential (each depends on the previous)

- [x] **Step 1** — `fix-10-georesult-constants.md` — Intern GeoResult failure singletons → `mvn compile`
- [x] **Step 2** — `fix-08-data-incomplete-status.md` — Add DATA_INCOMPLETE geo status → `mvn compile`
- [x] **Step 3** — `fix-07-remove-pending-v3-migration.md` — Remove PENDING + V3 migration → `mvn compile`
- [x] **Step 4** — `fix-06-iputils-extract.md` — Extract IpUtils.isPrivateAddress → `mvn compile`
- [x] **Step 5** — `fix-02-geoconfig-directory-guard.md` — Docker directory guard in GeoConfig → `mvn compile`
- [x] **Step 6** — `fix-05-geo-isenabled-skip-queries.md` — Skip geo queries when disabled → `mvn compile`
- [x] **Step 7** — `fix-04-analytics-str-count-helpers.md` — Extract str/count helpers in AnalyticsService → `mvn compile`
- [x] **Step 8** — `fix-03-remove-transactional.md` — Remove @Transactional from logClickAsync → `mvn compile`
- [x] **Step 9** — `fix-09-nginx-headers-dedup.md` — Move proxy_set_header to server block → `nginx -t`
- [x] **Step 10** — `fix-01-global-exception-handler.md` — Add GlobalExceptionHandler → `mvn test`

## Final gate

- [x] `cd backend && ./mvnw test` — full suite green (79 tests, 0 failures)
