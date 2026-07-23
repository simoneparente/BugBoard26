import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  const isApiRequest = req.url.startsWith(environment.apiBaseUrl) || req.url.startsWith('/');
  const clonedRequest = isApiRequest ? req.clone({ withCredentials: true }) : req;

  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      return throwError(() => error);
    }),
  );
};
