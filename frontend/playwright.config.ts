import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  retries: process.env['CI'] ? 2 : 0,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'pnpm start',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env['CI'],
    timeout: 120_000,
  },
  // Firefox is deliberately excluded: headless Firefox doesn't support WebGL
  // (https://bugzilla.mozilla.org/show_bug.cgi?id=1375585), and the app's default route
  // renders a MapLibre GL map, which requires it — every e2e test hangs/times out under
  // headless Firefox for this reason, not a bug in the app. Chromium and WebKit both support
  // headless WebGL and are sufficient cross-engine coverage.
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
  ],
});
