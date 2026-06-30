import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth-service';

/**
 * Guard to prevent unauthenticated users from accessing protected routes.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Since Signals are synchronous, we can immediately check the value
  if (authService.isAuthenticated()) {
    return true;
  }

  // Redirect to login if the user is not authenticated
  return router.createUrlTree(['/login']);
};
