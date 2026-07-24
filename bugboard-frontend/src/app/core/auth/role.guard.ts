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

/**
 * Guard that only allows users with the ADMIN role to access the route.
 * Redirects to the dashboard if the user is not an ADMIN.
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.userRole() === ROLES.ADMIN) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
