# Deploying to Render

This deploys two services from `render.yaml` (a Render [Blueprint](https://render.com/docs/blueprint-spec)):

- `transport-view-backend` — Spring Boot, built from `backend/Dockerfile`
- `transport-view-frontend` — Angular static site

The database is **not** Render-managed — see "Why an external database" below.

## Database: Neon (or any external Postgres with PostGIS)

`render.yaml` deliberately has no `databases:` block. Bring your own Postgres (this project uses [Neon](https://neon.tech), which has a real free tier and supports PostGIS):

1. Create a Neon project (or use an existing one) with a database.
2. In Neon's dashboard, enable PostGIS: SQL Editor → `CREATE EXTENSION IF NOT EXISTS postgis;` (or just let the app's own Flyway migration do it on first startup — it already runs that same statement).
3. Grab the connection details from Neon's dashboard (host, database name, user, password) — **don't paste them into chat, a file, or a commit.** They go directly into Render's dashboard in step 3 below.

### Why an external database, not Render-managed

Render only allows **one active free-tier database per workspace** — the first deploy attempt failed outright with "cannot have more than one active free tier database" because this account already had one elsewhere. Rather than pay for a second database (Render's cheapest paid tier is ~$6/mo) when a free one already exists, this uses that external database instead.

## One-time setup (you do this — it needs your Render account)

1. **Connect the repo.** In the [Render dashboard](https://dashboard.render.com), click **New > Blueprint**, select this GitHub repo, and point it at `render.yaml` on the `main` branch.
2. Render will show you the two services it's about to create and ask for the `sync: false` secrets (`DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) — fill these in from your Neon connection details, then click **Apply**.
3. If Render didn't prompt for those secrets during Apply, set them afterward: dashboard → `transport-view-backend` → Environment.
4. Wait for the backend to finish its first deploy (a few minutes — it's a multi-stage Docker build). The frontend build depends on `API_BASE_URL`, which is currently a **placeholder** in `render.yaml` — see step 5.
5. **Verify the assigned URLs.** Render names public URLs `https://<service-name>.onrender.com`, but if that exact name was already taken by someone else, Render appends a suffix instead. Check both services' actual URLs in the dashboard:
   - If `transport-view-backend`'s URL differs from the placeholder, update the frontend service's `API_BASE_URL` env var (dashboard → frontend service → Environment) to `https://<actual-backend-url>/api/v1`, then trigger a redeploy of the frontend.
   - If `transport-view-frontend`'s URL differs from the placeholder, update the backend service's `CORS_ALLOWED_ORIGINS` env var the same way, then redeploy the backend.
   (This step can't be automated — Render's Blueprint spec has no way to reference another service's *public* URL from within env vars, only its private network address, which isn't reachable from the browser.)
6. **Import the GTFS fixture.** The database starts empty — nothing is auto-imported. Grab the generated `ADMIN_GTFS_IMPORT_API_KEY` from the backend service's Environment tab, then:
   ```bash
   curl -X POST https://<your-backend-url>/api/v1/admin/gtfs/import \
     -H "X-Admin-Api-Key: <the key from the dashboard>" \
     -F "file=@backend/src/test/resources/gtfs/transtu-sample.zip"
   ```
   (Or the real TRANSTU feed — see [#2](https://github.com/rimzou12/transport-view/issues/2) for the source URL and the caveats about its data quality.)

## Cost and limits — read before relying on this

- **The backend is on Render's free plan**, which spins down after 15 minutes of inactivity (next request pays a ~30–60s cold-start cost waking it back up) and is capped at 750 instance-hours/month across your whole Render workspace. Switch `plan` to a paid tier (e.g. `starter`) if you want it always-responsive.
- The frontend (static site) has no such limits — Render's static hosting is free regardless of tier.
- Neon's free tier has its own limits (storage, compute hours, branches) — check [neon.tech/pricing](https://neon.tech/pricing) if this grows beyond a demo.

## Redeploying

Both services auto-deploy on push to `main` (per `branch: main` in `render.yaml`), consistent with [RELEASING.md](RELEASING.md) — a deploy happens exactly when you release. Pushing straight to `main` outside that process would also trigger a deploy; don't do that except through the normal `develop` → `main` release PR.
