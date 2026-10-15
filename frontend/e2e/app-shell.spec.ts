import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('app shell loads and shows the title', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('.app__title')).toContainText('Transport View');
});

test('app shell has no detectable accessibility violations', async ({ page }) => {
  await page.goto('/');
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});
