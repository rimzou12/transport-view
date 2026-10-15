import { test, expect } from '@playwright/test';

const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Unlike journey-search.spec.ts (mocked backend), this hits a real running backend + Postgres
 * with the same curated GTFS fixture the backend's own integration tests use
 * (backend/src/test/resources/gtfs/transtu-sample.zip) — marsa_plage -> carthage_présidence via
 * TGM. Skips gracefully if no backend is reachable, so local `pnpm test:e2e` without one running
 * doesn't just hang; CI (#6) starts a real backend + Postgres and imports the fixture first.
 */
test.beforeAll(async ({ request }) => {
  const healthCheck = await request.get(`${API_BASE_URL.replace('/api/v1', '')}/actuator/health`).catch(() => null);
  test.skip(!healthCheck?.ok(), 'No live backend reachable at localhost:8080 — skipping real-journey e2e');
});

test('searches a real journey against a live backend and database', async ({ page }) => {
  await page.goto('/');

  await page.getByLabel('From', { exact: true }).fill('marsa');
  await page.getByRole('option', { name: 'Marsa Plage' }).click();

  await page.getByLabel('To', { exact: true }).fill('carthage');
  await page.getByRole('option', { name: 'Carthage Presidence' }).click();

  await page.getByLabel('Departure time').fill('23:30');
  await page.getByRole('button', { name: 'Search' }).click();

  await expect(page.locator('app-itinerary-card').first()).toBeVisible({ timeout: 10_000 });
  await expect(page.locator('app-itinerary-card').first()).toContainText('ligne TGM');
});
