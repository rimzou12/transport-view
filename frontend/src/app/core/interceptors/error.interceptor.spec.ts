import type { HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { HttpResponse } from '@angular/common/http';
import { firstValueFrom, of, throwError, type Observable } from 'rxjs';
import { errorInterceptor } from './error.interceptor';

function handlerReturning(event: HttpEvent<unknown>): HttpHandlerFn {
  return () => of(event) as Observable<HttpEvent<unknown>>;
}

function handlerFailingWith(error: unknown): HttpHandlerFn {
  return () => throwError(() => error) as Observable<HttpEvent<unknown>>;
}

function noop(): void {
  // Suppresses console.error output in test runs.
}

describe('errorInterceptor', () => {
  const request = {} as HttpRequest<unknown>;

  beforeEach(() => {
    jest.spyOn(console, 'error').mockImplementation(noop);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('passes through a successful response unchanged', async () => {
    const response = new HttpResponse({ status: 200 });

    const event = await firstValueFrom(errorInterceptor(request, handlerReturning(response)));

    expect(event).toBe(response);
  });

  it('logs and rethrows when the downstream request errors', async () => {
    const failure = new Error('network down');

    await expect(firstValueFrom(errorInterceptor(request, handlerFailingWith(failure)))).rejects.toBe(failure);
    expect(console.error).toHaveBeenCalledWith('HTTP request failed', request.url, failure);
  });
});
