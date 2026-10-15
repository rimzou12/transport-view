import type { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/journey-search/components/journey-search-page/journey-search-page').then(
        (m) => m.JourneySearchPage,
      ),
  },
];
