import { test, expect, type Page } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

const STOPS_BY_QUERY: Record<string, unknown[]> = {
  marine: [{ id: 'STOP_A', name: 'Tunis Marine', latitude: 36.8008, longitude: 10.1815 }],
  marsa: [{ id: 'STOP_B', name: 'Marsa Plage', latitude: 36.87917, longitude: 10.325 }],
};

const ITINERARY_RESPONSE = [
  {
    legs: [
      {
        mode: 'METRO',
        fromStopName: 'Tunis Marine',
        fromStopLatitude: 36.8008,
        fromStopLongitude: 10.1815,
        toStopName: 'Marsa Plage',
        toStopLatitude: 36.87917,
        toStopLongitude: 10.325,
        departure: '08:00',
        arrival: '08:30',
        routeShortName: 'ligne TGM',
      },
    ],
    totalDurationSeconds: 1800,
    transferCount: 0,
  },
];

async function mockBackend(page: Page) {
  await page.route('**/api/v1/stops*', (route) => {
    const query = new URL(route.request().url()).searchParams.get('query') ?? '';
    const matchedKey = Object.keys(STOPS_BY_QUERY).find((key) => query.toLowerCase().includes(key));
    route.fulfill({ json: matchedKey ? STOPS_BY_QUERY[matchedKey] : [] });
  });

  await page.route('**/api/v1/itineraries', (route) => route.fulfill({ json: ITINERARY_RESPONSE }));
}

async function selectStop(page: Page, fieldLabel: string, query: string, optionName: string) {
  await page.getByLabel(fieldLabel, { exact: true }).fill(query);
  await page.getByRole('option', { name: optionName }).click();
}

test('searches a journey and renders the result', async ({ page }) => {
  await mockBackend(page);
  await page.goto('/');

  await selectStop(page, 'From', 'marine', 'Tunis Marine');
  await selectStop(page, 'To', 'marsa', 'Marsa Plage');
  await page.getByRole('button', { name: 'Search' }).click();

  await expect(page.locator('app-itinerary-card')).toHaveCount(1);
  await expect(page.locator('app-itinerary-card')).toContainText('ligne TGM');
});

test('journey search page with results has no detectable accessibility violations', async ({ page }) => {
  await mockBackend(page);
  await page.goto('/');

  await selectStop(page, 'From', 'marine', 'Tunis Marine');
  await selectStop(page, 'To', 'marsa', 'Marsa Plage');
  await page.getByRole('button', { name: 'Search' }).click();
  await expect(page.locator('app-itinerary-card')).toHaveCount(1);

  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});
