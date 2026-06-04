# Geo Analytics Setup

Enabling the geo feature populates the **Top Countries** and **Top Cities** charts in the analytics panel. Without it, the app runs normally and those charts are simply empty.

The feature is powered by MaxMind's free GeoLite2 City database. You need to register with MaxMind to download it.

## 1. Get the database

1. Create a free MaxMind account at [maxmind.com/en/geolite2/signup](https://www.maxmind.com/en/geolite2/signup).
2. In your account portal, go to **Manage License Keys** and generate a new key.
3. Download `GeoLite2-City.mmdb` from **Download Databases** → GeoLite2 City → mmdb format.

## 2. Place the file

Drop the downloaded file into the `geo/` directory at the project root:

```
geo/
  GeoLite2-City.mmdb   ← place it here
  .gitkeep
```

The `geo/` directory already exists (the `.gitkeep` is its placeholder). The file is `.gitignore`d, so it will not be committed.

## 3. Start the stack

No extra configuration needed — `docker-compose.yml` is pre-wired:

```bash
docker-compose up --build
```

It mounts `./geo/GeoLite2-City.mmdb` read-only into the backend container and sets `GEO_DB_PATH=/data/GeoLite2-City.mmdb` automatically.

If your database lives outside the `geo/` directory, override the host path before starting:

```bash
MAXMIND_DB_PATH=/absolute/path/to/GeoLite2-City.mmdb docker-compose up --build
```

## 4. Verify

Check the health endpoint:

```bash
curl -s http://localhost/actuator/health | jq '.components.geoResolver'
```

Expected when enabled:

```json
{ "status": "UP" }
```

If it shows `"status": "DEGRADED"`, the path is wrong or the file is missing (see [Troubleshooting](#troubleshooting)).

Once a click is recorded, open the analytics panel — the **Top Countries** and **Top Cities** charts will populate.

## Local development (outside Docker)

`application-dev.yml` already points `geo.db.path` at the bundled test database, so running with the `dev` profile gives you geo resolution out of the box (using sample data):

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

To use the real database locally, pass the path explicitly:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dgeo.db.path=../geo/GeoLite2-City.mmdb
```

Or export the env var before running:

```bash
export GEO_DB_PATH=../geo/GeoLite2-City.mmdb
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Running tests

Tests use a small bundled test database (`backend/src/test/resources/GeoLite2-City-Test.mmdb`) and require no external file. All geo-related tests pass without the production database:

```bash
cd backend
mvn test
```

The relevant test classes are `GeoResolverServiceTest`, `GeoAnalyticsIntegrationTest`, and `GeoDisabledIntegrationTest`.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `geoResolver` status is `DEGRADED` | File missing or path wrong | Confirm `geo/GeoLite2-City.mmdb` exists; check backend logs for a `WARN` from `GeoConfig` |
| Geo charts empty despite `UP` health | Clicks recorded before geo was enabled | Generate new clicks; only clicks recorded after enabling geo have country/city data |
| Private/loopback IPs show no location | Expected behaviour | Requests from `127.x`, `10.x`, `192.168.x`, etc. are always marked `PRIVATE`, not resolved |
| App fails to start | Not a geo issue | Geo failure is non-fatal; the app starts regardless — check other startup logs |
