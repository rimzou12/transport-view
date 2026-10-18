import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import type { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import type { Stop } from '@/shared/models/stop';

@Injectable({ providedIn: 'root' })
export class StopSearchService {
  private readonly http = inject(HttpClient);

  search(query: string): Observable<Stop[]> {
    return this.http.get<Stop[]>(`${environment.apiBaseUrl}/stops`, { params: { query } });
  }
}
