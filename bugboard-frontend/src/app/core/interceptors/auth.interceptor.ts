import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  const clonedRequest = req.clone({
    withCredentials: true,
  });

  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      return throwError(() => error);
    }),
  );
};
