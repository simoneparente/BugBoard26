import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthRequest, AuthResponse } from './auth.models';
import { catchError, tap, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  //TODO: Move this to environment.ts
  private readonly API_URL = 'http://localhost:8080/api/auth';

  private readonly currentUserSignal = signal<AuthResponse | null>(null);

  public readonly currentUser = this.currentUserSignal.asReadonly();
  public readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  public readonly userRole = computed(() => this.currentUserSignal()?.role ?? null);

  /**
   * Authenticates the user and updates the signal.
   */
  public login(credentials: AuthRequest) {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(tap((response) => this.currentUserSignal.set(response)));
  }

  /**
   * Checks the current session and updates the signal.
   */
  public checkSession(): Observable<AuthResponse | null> {
    return this.http.get<AuthResponse>(`${this.API_URL}/me`).pipe(
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
    this.currentUserSignal.set(null);
  }
}
