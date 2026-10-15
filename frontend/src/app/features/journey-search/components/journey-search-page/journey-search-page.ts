import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatAutocompleteModule, type MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { debounceTime, distinctUntilChanged, map, merge, of, Subject, switchMap } from 'rxjs';
import { JourneyMap } from '@/features/journey-search/components/journey-map/journey-map';
import { ItineraryList } from '@/features/journey-search/components/itinerary-list/itinerary-list';
import { JourneySearchService } from '@/features/journey-search/services/journey-search.service';
import { StopSearchService } from '@/features/journey-search/services/stop-search.service';
import type { Itinerary } from '@/shared/models/itinerary';
import type { Stop } from '@/shared/models/stop';

const SEARCH_DEBOUNCE_MS = 300;
const MIN_QUERY_LENGTH = 2;

@Component({
  selector: 'app-journey-search-page',
  imports: [
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    JourneyMap,
    ItineraryList,
  ],
  templateUrl: './journey-search-page.html',
  styleUrl: './journey-search-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JourneySearchPage {
  private readonly stopSearchService = inject(StopSearchService);
  private readonly journeySearchService = inject(JourneySearchService);

  protected readonly originControl = new FormControl('', { nonNullable: true });
  protected readonly destinationControl = new FormControl('', { nonNullable: true });
  protected readonly departureTimeControl = new FormControl('', { nonNullable: true });

  private readonly originBrowseTrigger = new Subject<void>();
  private readonly destinationBrowseTrigger = new Subject<void>();

  protected readonly originSuggestions = toSignal(
    this.suggestionsFor(this.originControl, this.originBrowseTrigger),
    { initialValue: [] },
  );
  protected readonly destinationSuggestions = toSignal(
    this.suggestionsFor(this.destinationControl, this.destinationBrowseTrigger),
    { initialValue: [] },
  );

  protected readonly selectedOrigin = signal<Stop | null>(null);
  protected readonly selectedDestination = signal<Stop | null>(null);

  protected readonly itineraries = signal<Itinerary[]>([]);
  protected readonly selectedItinerary = signal<Itinerary | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly searched = signal(false);

  /**
   * Shows the full stop list on focus, before the user types anything — so they can see what's
   * actually available (this dataset only covers metro + TGM, see #7) instead of guessing a
   * name that doesn't exist, which previously produced an empty dropdown with no explanation.
   */
  protected browseOriginOnFocus(): void {
    this.originBrowseTrigger.next();
  }

  protected browseDestinationOnFocus(): void {
    this.destinationBrowseTrigger.next();
  }

  protected selectOrigin(event: MatAutocompleteSelectedEvent): void {
    this.selectedOrigin.set(this.findSuggestion(this.originSuggestions(), event.option.value));
  }

  protected selectDestination(event: MatAutocompleteSelectedEvent): void {
    this.selectedDestination.set(this.findSuggestion(this.destinationSuggestions(), event.option.value));
  }

  /**
   * Autocomplete only registers a selection via an explicit option click/Enter. If the user
   * types an exact stop name and then tabs or clicks away without that explicit selection, the
   * field looks filled but nothing was actually chosen — this resolves that case on blur, and
   * clears any stale selection that no longer matches the typed text (see #16).
   */
  protected resolveOriginOnBlur(): void {
    this.selectedOrigin.set(this.resolveExactMatch(this.originControl, this.originSuggestions(), this.selectedOrigin()));
  }

  protected resolveDestinationOnBlur(): void {
    this.selectedDestination.set(
      this.resolveExactMatch(this.destinationControl, this.destinationSuggestions(), this.selectedDestination()),
    );
  }

  private resolveExactMatch(control: FormControl<string>, suggestions: readonly Stop[], current: Stop | null): Stop | null {
    if (current?.name === control.value) {
      return current;
    }
    return this.findSuggestion(suggestions, control.value);
  }

  private findSuggestion(suggestions: readonly Stop[], name: string): Stop | null {
    const normalized = name.trim().toLowerCase();
    return suggestions.find((stop) => stop.name.toLowerCase() === normalized) ?? null;
  }

  protected onSubmit(event: SubmitEvent): void {
    event.preventDefault();
    this.search();
  }

  protected search(): void {
    const origin = this.selectedOrigin();
    const destination = this.selectedDestination();
    if (!origin || !destination || this.loading()) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.journeySearchService
      .search({
        originLatitude: origin.latitude,
        originLongitude: origin.longitude,
        destinationLatitude: destination.latitude,
        destinationLongitude: destination.longitude,
        departureTime: this.departureTimeControl.value || undefined,
      })
      .subscribe({
        next: (itineraries) => {
          this.itineraries.set(itineraries);
          this.selectedItinerary.set(itineraries[0] ?? null);
          this.loading.set(false);
          this.searched.set(true);
        },
        error: () => {
          this.error.set('Could not search journeys right now. Please try again.');
          this.loading.set(false);
          this.searched.set(true);
        },
      });
  }

  private suggestionsFor(control: FormControl<string>, browseTrigger: Subject<void>) {
    const typedQueries = control.valueChanges.pipe(debounceTime(SEARCH_DEBOUNCE_MS), distinctUntilChanged());
    const browseRequests = browseTrigger.pipe(map(() => control.value));

    return merge(typedQueries, browseRequests).pipe(
      switchMap((query) => (query.length === 0 || query.length >= MIN_QUERY_LENGTH ? this.stopSearchService.search(query) : of([]))),
    );
  }
}
