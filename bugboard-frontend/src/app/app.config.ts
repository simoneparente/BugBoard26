import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/auth/auth-service';
import { firstValueFrom, Observable } from 'rxjs';

export function initializeApp(authService: AuthService): () => Observable<any> {
  return () => authService.checkSession();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    provideAppInitializer(async () => {
      const authService = inject(AuthService);
      try {
        // firstValueFrom forces the Observable to resolve and allows us to catch errors during initialization.
        await firstValueFrom(authService.checkSession());
      } catch (error) {
        // If the session check fails, we log a warning. This could be due to network issues or the user not being authenticated.
        console.warn('Session check failed during initialization.');
      }
    }),
  ],
};
