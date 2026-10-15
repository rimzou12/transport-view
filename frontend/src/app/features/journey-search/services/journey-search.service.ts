import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import type { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import type { Itinerary } from '@/shared/models/itinerary';
import type { ItinerarySearchRequest } from './itinerary-search-request';

@Injectable({ providedIn: 'root' })
export class JourneySearchService {
  private readonly http = inject(HttpClient);

  search(request: ItinerarySearchRequest): Observable<Itinerary[]> {
    return this.http.post<Itinerary[]>(`${environment.apiBaseUrl}/itineraries`, request);
  }
}
