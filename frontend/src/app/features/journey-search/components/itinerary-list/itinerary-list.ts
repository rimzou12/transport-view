import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ItineraryCard } from '@/features/journey-search/components/itinerary-card/itinerary-card';
import type { Itinerary } from '@/shared/models/itinerary';

@Component({
  selector: 'app-itinerary-list',
  imports: [MatProgressSpinnerModule, ItineraryCard],
  templateUrl: './itinerary-list.html',
  styleUrl: './itinerary-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ItineraryList {
  readonly itineraries = input<readonly Itinerary[]>([]);
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  readonly searched = input(false);
}
