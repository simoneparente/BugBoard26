import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth-service';
import { ROLES } from '../roles.model';

/**
 * Guard that prevents EXTERNAL (read-only/stakeholder) users from accessing
 * routes reserved for ADMIN and TECHNICAL users (e.g. create issue, reports).
 * Redirects to the dashboard with an access-denied state if the user is EXTERNAL.
 */
export const notExternalGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.userRole() === ROLES.EXTERNAL) {
    // Redirect read-only users away from write-only routes
    return router.createUrlTree(['/dashboard']);
  }

  return true;
};
