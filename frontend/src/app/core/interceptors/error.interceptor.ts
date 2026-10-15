import type { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: unknown) => {
      console.error('HTTP request failed', req.url, error);
      return throwError(() => error);
    }),
  );
};
