import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, tap, Observable, of } from 'rxjs';
import { AuthRequest, AuthResponse, UserRegistrationRequest, UserResponse } from './auth.models';
import { environment } from '../../../environments/environment';
import { NotificationService } from '../services/notification.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private notificationService = inject(NotificationService);
  private router = inject(Router);
  private readonly AUTH_API_URL = environment.authApiUrl;
  private readonly USER_API_URL = environment.userApiUrl;

  private readonly currentUserSignal = signal<AuthResponse | null>(null);

  public readonly currentUser = this.currentUserSignal.asReadonly();
  public readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  public readonly userRole = computed(() => this.currentUserSignal()?.role ?? null);

  /**
   * Authenticates the user and updates the signal.
   */
  public login(credentials: AuthRequest) {
    return this.http
      .post<AuthResponse>(`${this.AUTH_API_URL}/login`, credentials)
      .pipe(tap((response) => this.currentUserSignal.set(response)));
  }

  /**
   * Registers a new user.
   * Note: This does not automatically log the user in.
   */
  public register(payload: UserRegistrationRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.USER_API_URL}/register`, payload);
  }

  /**
   * Checks the current session and updates the signal.
   */
  public checkSession(): Observable<AuthResponse | null> {
    return this.http.get<AuthResponse>(`${this.AUTH_API_URL}/me`).pipe(
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
    this.http.post(`${this.AUTH_API_URL}/logout`, {}).subscribe({
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
