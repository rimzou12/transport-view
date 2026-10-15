import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { JourneySearchPage } from './journey-search-page';
import { JourneySearchService } from '@/features/journey-search/services/journey-search.service';
import { StopSearchService } from '@/features/journey-search/services/stop-search.service';
import type { Itinerary } from '@/shared/models/itinerary';
import type { Stop } from '@/shared/models/stop';

const tunisMarine: Stop = { id: 'STOP_A', name: 'Tunis Marine', latitude: 36.8008, longitude: 10.1815 };
const marsaPlage: Stop = { id: 'STOP_B', name: 'Marsa Plage', latitude: 36.87917, longitude: 10.325 };
const sampleItinerary: Itinerary = {
  legs: [
    {
      mode: 'METRO',
      fromStopName: tunisMarine.name,
      fromStopLatitude: tunisMarine.latitude,
      fromStopLongitude: tunisMarine.longitude,
      toStopName: marsaPlage.name,
      toStopLatitude: marsaPlage.latitude,
      toStopLongitude: marsaPlage.longitude,
      departure: '08:00',
      arrival: '08:30',
      routeShortName: 'ligne TGM',
    },
  ],
  totalDurationSeconds: 1800,
  transferCount: 0,
};

async function configureTestBed(stopSearchResults: Stop[], journeySearchResult: Itinerary[] | 'error') {
  const stopSearchService = { search: jest.fn().mockReturnValue(of(stopSearchResults)) };
  const journeySearchResponse =
    journeySearchResult === 'error' ? throwError(() => new Error('boom')) : of(journeySearchResult);
  const journeySearchService = { search: jest.fn().mockReturnValue(journeySearchResponse) };

  await TestBed.configureTestingModule({
    imports: [JourneySearchPage],
    providers: [
      provideAnimationsAsync(),
      { provide: StopSearchService, useValue: stopSearchService },
      { provide: JourneySearchService, useValue: journeySearchService },
    ],
  }).compileComponents();

  return { stopSearchService, journeySearchService };
}

async function submitForm(fixture: ReturnType<typeof TestBed.createComponent>) {
  const form = (fixture.nativeElement as HTMLElement).querySelector('form') as HTMLFormElement;
  form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
  await fixture.whenStable();
}

async function submitWithSelectedStops(fixture: ReturnType<typeof TestBed.createComponent>) {
  fixture.componentInstance['selectedOrigin'].set(tunisMarine);
  fixture.componentInstance['selectedDestination'].set(marsaPlage);
  fixture.detectChanges();
  await submitForm(fixture);
}

describe('JourneySearchPage search flow', () => {
  it('disables the search button until both an origin and a destination are selected', async () => {
    await configureTestBed([tunisMarine, marsaPlage], []);
    const fixture = TestBed.createComponent(JourneySearchPage);
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const submitButton = compiled.querySelector('button[type="submit"]') as HTMLButtonElement;
    expect(submitButton.disabled).toBe(true);
  });

  it('searches and renders results once origin and destination are selected', async () => {
    await configureTestBed([tunisMarine, marsaPlage], [sampleItinerary]);
    const fixture = TestBed.createComponent(JourneySearchPage);
    await submitWithSelectedStops(fixture);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelectorAll('app-itinerary-card')).toHaveLength(1);
  });

  it('shows an error message when the search fails', async () => {
    await configureTestBed([], 'error');
    const fixture = TestBed.createComponent(JourneySearchPage);
    await submitWithSelectedStops(fixture);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('[role="alert"]')?.textContent).toContain('Could not search journeys');
  });

  it('does not search when the form is submitted without both stops selected', async () => {
    const { journeySearchService } = await configureTestBed([], []);
    const fixture = TestBed.createComponent(JourneySearchPage);
    fixture.detectChanges();
    await submitForm(fixture);

    expect(journeySearchService.search).not.toHaveBeenCalled();
  });

  it('clears the selected itinerary when the search returns no results', async () => {
    await configureTestBed([], []);
    const fixture = TestBed.createComponent(JourneySearchPage);
    await submitWithSelectedStops(fixture);

    expect(fixture.componentInstance['selectedItinerary']()).toBeNull();
  });
});

describe('JourneySearchPage autocomplete', () => {
  afterEach(() => jest.useRealTimers());

  it('selects a stop from autocomplete suggestions as the user types', async () => {
    jest.useFakeTimers();
    await configureTestBed([tunisMarine], []);
    const fixture = TestBed.createComponent(JourneySearchPage);
    const component = fixture.componentInstance;

    component['originControl'].setValue('mar');
    jest.advanceTimersByTime(400);
    await fixture.whenStable();
    expect(component['originSuggestions']()).toEqual([tunisMarine]);

    component['selectOrigin']({ option: { value: tunisMarine.name } } as never);
    expect(component['selectedOrigin']()).toEqual(tunisMarine);
  });

  it('selects a destination stop from autocomplete suggestions', async () => {
    jest.useFakeTimers();
    await configureTestBed([marsaPlage], []);
    const fixture = TestBed.createComponent(JourneySearchPage);
    const component = fixture.componentInstance;

    component['destinationControl'].setValue('mar');
    jest.advanceTimersByTime(400);
    await fixture.whenStable();
    expect(component['destinationSuggestions']()).toEqual([marsaPlage]);

    component['selectDestination']({ option: { value: marsaPlage.name } } as never);
    expect(component['selectedDestination']()).toEqual(marsaPlage);
  });

  it('does not query the backend for a query shorter than the minimum length', async () => {
    jest.useFakeTimers();
    const { stopSearchService } = await configureTestBed([tunisMarine], []);
    const fixture = TestBed.createComponent(JourneySearchPage);

    fixture.componentInstance['originControl'].setValue('a');
    jest.advanceTimersByTime(400);
    await fixture.whenStable();

    expect(stopSearchService.search).not.toHaveBeenCalled();
  });
});
