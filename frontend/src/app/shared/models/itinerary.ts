import type { TransportMode } from './transport-mode';

export interface Leg {
  readonly mode: TransportMode;
  readonly fromStopName: string;
  readonly fromStopLatitude: number;
  readonly fromStopLongitude: number;
  readonly toStopName: string;
  readonly toStopLatitude: number;
  readonly toStopLongitude: number;
  readonly departure: string;
  readonly arrival: string;
  readonly routeShortName: string | null;
}

export interface Itinerary {
  readonly legs: readonly Leg[];
  readonly totalDurationSeconds: number;
  readonly transferCount: number;
}
