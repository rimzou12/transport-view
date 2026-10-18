// Angular's environment.ts is baked into the bundle at build time via fileReplacements, but a
// static-site host (Render) only knows the backend's actual deployed URL as a build-time env
// var — it can't be a fixed value in the committed file. This writes environment.ts just before
// `ng build` runs, reading that URL from the environment.
import { writeFileSync } from 'node:fs';

const apiBaseUrl = process.env.API_BASE_URL;

if (!apiBaseUrl) {
  console.error('API_BASE_URL is not set — refusing to build a production bundle pointing nowhere.');
  process.exit(1);
}

const content = `export const environment = {
  production: true,
  apiBaseUrl: '${apiBaseUrl}',
};
`;

writeFileSync(new URL('../src/environments/environment.ts', import.meta.url), content);
console.log(`Wrote environment.ts with apiBaseUrl=${apiBaseUrl}`);
