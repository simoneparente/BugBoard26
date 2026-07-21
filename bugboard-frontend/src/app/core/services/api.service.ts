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
import { Page } from '../page.model';
import { ProjectResponse } from '../project.model';

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

  readonly projects = {
    getAll: (page: number, size: number) =>
      this.http.get<Page<ProjectResponse>>(`${this.baseUrl}/projects`, {
        params: { page: page.toString(), size: size.toString() },
      }),
    getById: (id: string) =>
      this.http.get<ProjectResponse>(`${this.baseUrl}/projects/${id}`),
    create: (name: string, description: string) =>
      this.http.post<ProjectResponse>(`${this.baseUrl}/projects`, { name, description }),
    delete: (id: string) =>
      this.http.delete<void>(`${this.baseUrl}/projects/${id}`),
  };

  readonly reports = {
    getMonthlyReport: (projectId: string) =>
      this.http.get<any>(`${this.baseUrl}/reports/${projectId}`),
  };
}
