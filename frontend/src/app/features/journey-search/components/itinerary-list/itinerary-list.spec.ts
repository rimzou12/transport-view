import { TestBed } from '@angular/core/testing';
import { ItineraryList } from './itinerary-list';
import type { Itinerary } from '@/shared/models/itinerary';

const sampleItinerary: Itinerary = {
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
};

async function renderWith(inputs: Partial<{ loading: boolean; error: string; itineraries: Itinerary[]; searched: boolean }>) {
  const fixture = TestBed.createComponent(ItineraryList);
  for (const [key, value] of Object.entries(inputs)) {
    fixture.componentRef.setInput(key, value);
  }
  await fixture.whenStable();
  return fixture.nativeElement as HTMLElement;
}

describe('ItineraryList', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ItineraryList] }).compileComponents();
  });

  it('shows a loading indicator while searching', async () => {
    const compiled = await renderWith({ loading: true });
    expect(compiled.querySelector('[role="status"]')).not.toBeNull();
  });

  it('shows an error message when the search failed', async () => {
    const compiled = await renderWith({ error: 'Something went wrong' });
    expect(compiled.querySelector('[role="alert"]')?.textContent).toContain('Something went wrong');
  });

  it('renders a card per itinerary', async () => {
    const compiled = await renderWith({ itineraries: [sampleItinerary] });
    expect(compiled.querySelectorAll('app-itinerary-card')).toHaveLength(1);
  });

  it('shows an empty-state message only after a search with no results', async () => {
    const compiled = await renderWith({ searched: true });
    expect(compiled.textContent).toContain('No journeys found');
  });

  it('shows nothing before a search has been made', async () => {
    const compiled = await renderWith({});
    expect(compiled.textContent?.trim()).toBe('');
  });
});
