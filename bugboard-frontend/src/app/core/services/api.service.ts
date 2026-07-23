import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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
import { IssueResponse } from '../issue.model';

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
    getById: (id: string) => this.http.get<ProjectResponse>(`${this.baseUrl}/projects/${id}`),
    create: (name: string, description: string) =>
      this.http.post<ProjectResponse>(`${this.baseUrl}/projects`, { name, description }),
    delete: (id: string) => this.http.delete<void>(`${this.baseUrl}/projects/${id}`),
  };

  readonly issues = {
    getByProject: (
      projectId: string,
      status: string = 'ALL',
      priority: string = 'ALL',
      page: number = 0,
      size: number = 20,
      sortField: string = 'createdAt',
      sortDirection: string = 'desc',
    ) => {
      let params = new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString())
        .set('sort', `${sortField},${sortDirection}`);
      if (status && status !== 'ALL') {
        params = params.set('status', status);
      }
      if (priority && priority !== 'ALL') {
        params = params.set('priority', priority);
      }

      const url = `${this.baseUrl}/projects/${projectId}/issues`;

      return this.http.get<Page<IssueResponse>>(`${this.baseUrl}/projects/${projectId}/issues`, {
        params,
      });
    },
    create: (projectId: string, payload: any) =>
      this.http.post<IssueResponse>(`${this.baseUrl}/projects/${projectId}/issues`, payload),
    uploadAttachment: (issueId: string, file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      //TODO: Use a better type for the response
      return this.http.post<any>(`${this.baseUrl}/attachments/issue/${issueId}`, formData);
    },
  };

  readonly reports = {
    getMonthlyReport: (projectId: string) =>
      this.http.get<any>(`${this.baseUrl}/reports/${projectId}`),
  };
}
