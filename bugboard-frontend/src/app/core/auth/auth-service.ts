import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, tap, Observable, of } from 'rxjs';
import { AuthRequest, AuthResponse, UserRegistrationRequest, UserResponse } from './auth.models';
import { NotificationService } from '../services/notification.service';
import { ApiService } from '../services/api.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiService = inject(ApiService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  private readonly currentUserSignal = signal<AuthResponse | null>(null);

  public readonly currentUser = this.currentUserSignal.asReadonly();
  public readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  public readonly userRole = computed(() => this.currentUserSignal()?.role ?? null);

  /**
   * Authenticates the user and updates the signal.
   */
  public login(credentials: AuthRequest) {
    return this.apiService.auth
      .login(credentials)
      .pipe(tap((response) => this.currentUserSignal.set(response)));
  }

  /**
   * Registers a new user.
   * Note: This does not automatically log the user in.
   */
  public register(payload: UserRegistrationRequest): Observable<UserResponse> {
    return this.apiService.users.register(payload);
  }

  /**
   * Checks the current session and updates the signal.
   */
  public checkSession(): Observable<AuthResponse | null> {
    return this.apiService.auth.me().pipe(
      tap((response) => this.currentUserSignal.set(response)),
      catchError(() => {
        //If cookie is missing or invalid, clear state
        this.currentUserSignal.set(null);
        return of(null);
      }),
    );
  }

  /**
   * Logs out the user and clears the signal.
   */
  public logout() {
    this.apiService.auth.logout().subscribe({
      next: () => {
        this.currentUserSignal.set(null);
        this.notificationService.showSuccess('Success', 'Logged out successfully');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.notificationService.showError(
          'Error',
          'There was an error logging out. Redirecting to login page.',
        );
        this.currentUserSignal.set(null);
        this.router.navigate(['/login']);
      },
    });
  }
}
