import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import type { TransportMode } from '@/shared/models/transport-mode';

const ICON_BY_MODE: Record<TransportMode, string> = {
  METRO: 'tram',
  BUS: 'directions_bus',
  TAXI: 'local_taxi',
  WALK: 'directions_walk',
};

const LABEL_BY_MODE: Record<TransportMode, string> = {
  METRO: 'Metro',
  BUS: 'Bus',
  TAXI: 'Taxi',
  WALK: 'Walk',
};

@Component({
  selector: 'app-transport-mode-icon',
  imports: [MatIconModule],
  template: `<mat-icon [attr.aria-label]="label()">{{ icon() }}</mat-icon>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransportModeIcon {
  readonly mode = input.required<TransportMode>();

  protected readonly icon = computed(() => ICON_BY_MODE[this.mode()]);
  protected readonly label = computed(() => LABEL_BY_MODE[this.mode()]);
}
