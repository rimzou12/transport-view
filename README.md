# Transport View — Tunis

A multi-modal journey planner for Tunis: search a departure and destination and see the available transport options (metro léger, bus, taxi) to get there, including multi-leg itineraries with transfers.

## Stack

- **Backend**: Java 25, Spring Boot, PostgreSQL + PostGIS, Flyway, hexagonal architecture.
- **Frontend**: Angular (latest, standalone), PrimeNG, MapLibre GL + OpenStreetMap.
- **Data**: TRANSTU GTFS feed (bus, metro léger, TGM) — see [catalogue-data.transport.tn](https://catalogue-data.transport.tn/dataset/horaires-des-voyages-de-la-transtu-gtfs).
- **Routing**: custom RAPTOR-based itinerary engine (`backend/.../domain/routing`).

## Repository layout

```
transport-view/
├── backend/    # Spring Boot application (hexagonal: domain / application / infrastructure)
├── frontend/   # Angular application (core / features / shared)
└── .github/workflows/   # CI
```

## Workflow (ADLC)

```
Brainstorm → Plan (GitHub issue) → Dev (TDD, feature branch) → Self-review → PR → QA → Merge to develop → Release (develop → main)
```

- Branches: `feat/<issue-id>-slug`, `fix/<issue-id>-slug`, always off `develop`.
- `develop` is the integration branch; `main` only receives tagged releases from `develop`.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) (enforced by commitlint via Husky).

## Running locally

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Requires `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` env vars (see `application.yml` for defaults) pointing at a PostgreSQL instance with the PostGIS extension available.

#### Importing GTFS data

Set `ADMIN_GTFS_IMPORT_API_KEY` (empty/unset = the endpoint always rejects), download the TRANSTU feed yourself (see [issue #2](https://github.com/rimzou12/transport-view/issues/2) for the source URL — the portal's TLS cert is currently expired, so this is a manual, out-of-band step rather than something the app fetches itself), then:

```bash
curl -X POST http://localhost:8080/api/v1/admin/gtfs/import \
  -H "X-Admin-Api-Key: $ADMIN_GTFS_IMPORT_API_KEY" \
  -F "file=@transtu_gtfs.zip"
```

Only metro + TGM are imported (bus is filtered out — see [issue #7](https://github.com/rimzou12/transport-view/issues/7)). Safe to re-run (upserts).

### Frontend

```bash
cd frontend
pnpm install
pnpm start
```

## Testing

| | Backend | Frontend |
|---|---|---|
| Unit | `./mvnw test` (JUnit5 + Mockito, pure domain) | `pnpm test` (Jest) |
| Integration | `./mvnw verify` (Testcontainers: real Postgres+PostGIS) | `pnpm test` (TestBed component tests) |
| E2E | — | `pnpm test:e2e` (Playwright, + axe-core a11y checks) |
| Coverage | — | `pnpm test:coverage` (90% threshold) |
