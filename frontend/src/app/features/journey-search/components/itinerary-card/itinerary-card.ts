import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { TransportModeIcon } from '@/shared/components/transport-mode-icon/transport-mode-icon';
import { DurationPipe } from '@/shared/pipes/duration.pipe';
import type { Itinerary } from '@/shared/models/itinerary';

@Component({
  selector: 'app-itinerary-card',
  imports: [MatCardModule, MatChipsModule, TransportModeIcon, DurationPipe],
  templateUrl: './itinerary-card.html',
  styleUrl: './itinerary-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ItineraryCard {
  readonly itinerary = input.required<Itinerary>();
}
