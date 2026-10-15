import { HttpClient } from '@angular/common/http';
import { Injector, runInInjectionContext } from '@angular/core';
import { firstValueFrom, of } from 'rxjs';
import { StopSearchService } from './stop-search.service';
import type { Stop } from '@/shared/models/stop';

function serviceWithMockHttp(httpClient: HttpClient): StopSearchService {
  const injector = Injector.create({ providers: [{ provide: HttpClient, useValue: httpClient }] });
  return runInInjectionContext(injector, () => new StopSearchService());
}

describe('StopSearchService', () => {
  it('queries the stops endpoint with the given search term', async () => {
    const expected: Stop[] = [{ id: 'STOP_1', name: 'Tunis Marine', latitude: 36.8, longitude: 10.18 }];
    const httpClient = { get: jest.fn().mockReturnValue(of(expected)) } as unknown as HttpClient;
    const service = serviceWithMockHttp(httpClient);

    const result = await firstValueFrom(service.search('marine'));

    expect(result).toEqual(expected);
    expect(httpClient.get).toHaveBeenCalledWith(expect.stringContaining('/stops'), {
      params: { query: 'marine' },
    });
  });
});
