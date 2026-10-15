import { HttpClient } from '@angular/common/http';
import { Injector, runInInjectionContext } from '@angular/core';
import { firstValueFrom, of } from 'rxjs';
import { JourneySearchService } from './journey-search.service';
import type { Itinerary } from '@/shared/models/itinerary';

function serviceWithMockHttp(httpClient: HttpClient): JourneySearchService {
  const injector = Injector.create({ providers: [{ provide: HttpClient, useValue: httpClient }] });
  return runInInjectionContext(injector, () => new JourneySearchService());
}

describe('JourneySearchService', () => {
  it('posts the search request to the itineraries endpoint', async () => {
    const expected: Itinerary[] = [{ legs: [], totalDurationSeconds: 600, transferCount: 0 }];
    const httpClient = { post: jest.fn().mockReturnValue(of(expected)) } as unknown as HttpClient;
    const service = serviceWithMockHttp(httpClient);

    const result = await firstValueFrom(
      service.search({ originLatitude: 36.8, originLongitude: 10.18, destinationLatitude: 36.81, destinationLongitude: 10.19 }),
    );

    expect(result).toEqual(expected);
    expect(httpClient.post).toHaveBeenCalledWith(
      expect.stringContaining('/itineraries'),
      expect.objectContaining({ originLatitude: 36.8 }),
    );
  });
});
