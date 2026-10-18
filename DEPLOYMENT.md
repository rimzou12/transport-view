# Deploying to Render

This deploys three services from `render.yaml` (a Render [Blueprint](https://render.com/docs/blueprint-spec)):

- `transport-view-db` — managed PostgreSQL (PostGIS enabled via the existing Flyway migration)
- `transport-view-backend` — Spring Boot, built from `backend/Dockerfile`
- `transport-view-frontend` — Angular static site

## One-time setup (you do this — it needs your Render account)

1. **Connect the repo.** In the [Render dashboard](https://dashboard.render.com), click **New > Blueprint**, select this GitHub repo, and point it at `render.yaml` on the `main` branch.
2. Render will show you the three services it's about to create from the file. Review and click **Apply**.
3. Wait for the database and backend to finish their first deploy (backend build takes a few minutes — it's a multi-stage Docker build). The frontend build depends on `API_BASE_URL`, which is currently a **placeholder** in `render.yaml` — see step 4.
4. **Verify the assigned URLs.** Render names public URLs `https://<service-name>.onrender.com`, but if that exact name was already taken by someone else, Render appends a suffix instead. Check both services' actual URLs in the dashboard:
   - If `transport-view-backend`'s URL differs from the placeholder, update the frontend service's `API_BASE_URL` env var (dashboard → frontend service → Environment) to `https://<actual-backend-url>/api/v1`, then trigger a redeploy of the frontend.
   - If `transport-view-frontend`'s URL differs from the placeholder, update the backend service's `CORS_ALLOWED_ORIGINS` env var the same way, then redeploy the backend.
   (This step can't be automated — Render's Blueprint spec has no way to reference another service's *public* URL from within env vars, only its private network address, which isn't reachable from the browser.)
5. **Import the GTFS fixture.** The database starts empty — nothing is auto-imported. Grab the generated `ADMIN_GTFS_IMPORT_API_KEY` from the backend service's Environment tab, then:
   ```bash
   curl -X POST https://<your-backend-url>/api/v1/admin/gtfs/import \
     -H "X-Admin-Api-Key: <the key from the dashboard>" \
     -F "file=@backend/src/test/resources/gtfs/transtu-sample.zip"
   ```
   (Or the real TRANSTU feed — see [#2](https://github.com/rimzou12/transport-view/issues/2) for the source URL and the caveats about its data quality.)

## Cost and limits — read before relying on this

- **The database is on a paid plan** (`0.5c-1g`, ~$6/mo) — not "free". Render only allows **one active free-tier database per workspace**, so if you already have one elsewhere (as happened on first deploy — see the PR that fixed this), the free plan fails outright with "cannot have more than one active free tier database". Separately, free databases also expire 30 days after creation (+ a 14-day grace period) and are then deleted — not viable for a lasting deployment either way.
- **The backend is on Render's free plan**, which spins down after 15 minutes of inactivity (next request pays a ~30–60s cold-start cost waking it back up) and is capped at 750 instance-hours/month across your whole Render workspace. Switch `plan` to a paid tier (e.g. `starter`) if you want it always-responsive.
- The frontend (static site) has no such limits — Render's static hosting is free regardless of tier.

## Redeploying

Both services auto-deploy on push to `main` (per `branch: main` in `render.yaml`), consistent with [RELEASING.md](RELEASING.md) — a deploy happens exactly when you release. Pushing straight to `main` outside that process would also trigger a deploy; don't do that except through the normal `develop` → `main` release PR.
