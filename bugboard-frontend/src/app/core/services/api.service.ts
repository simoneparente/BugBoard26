import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {
  AuthRequest,
  AuthResponse,
  UserRegistrationRequest,
  UserResponse,
} from '../auth/auth.models';
import { InvitationResponse } from '../invitation.model';
import { ROLES } from '../roles.model';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  readonly auth = {
    login: (credentials: AuthRequest) =>
      this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, credentials),
    logout: () => this.http.post(`${this.baseUrl}/auth/logout`, {}),
    me: () => this.http.get<AuthResponse>(`${this.baseUrl}/auth/me`),
  };

  readonly users = {
    register: (payload: UserRegistrationRequest) =>
      this.http.post<UserResponse>(`${this.baseUrl}/users/register`, payload),
  };

  readonly invitations = {
    create: (payload: { role: ROLES }) =>
      this.http.post<InvitationResponse>(`${this.baseUrl}/invitations`, payload),
  };
}
