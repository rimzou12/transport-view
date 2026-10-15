export interface ItinerarySearchRequest {
  readonly originLatitude: number;
  readonly originLongitude: number;
  readonly destinationLatitude: number;
  readonly destinationLongitude: number;
  readonly departureTime?: string;
}
