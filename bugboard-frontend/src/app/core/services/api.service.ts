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
    getAll: () => this.http.get<UserResponse[]>(`${this.baseUrl}/users`),
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
    getById: (projectId: string, issueId: string) =>
      this.http.get<IssueResponse>(`${this.baseUrl}/projects/${projectId}/issues/${issueId}`),
    getByProject: (projectId: string, page = 0, size = 10) =>
      this.http.get<Page<IssueResponse>>(`${this.baseUrl}/projects/${projectId}/issues`, {
        params: { page: page.toString(), size: size.toString() },
      }),
    assign: (projectId: string, issueId: string, assigneeId: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectId}/issues/${issueId}/assign`,
        null,
        { params: { assigneeId } },
      ),
    removeAssignee: (projectId: string, issueId: string) =>
      this.http.delete<IssueResponse>(
        `${this.baseUrl}/projects/${projectId}/issues/${issueId}/assignee`,
      ),
    setStatus: (projectId: string, issueId: string, status: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectId}/issues/${issueId}/status`,
        null,
        { params: { status } },
      ),
    startProgress: (projectId: string, issueId: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectId}/issues/${issueId}/start-progress`,
        null,
      ),
    accept: (projectId: string, issueId: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectId}/issues/${issueId}/accept`,
        null,
      ),
    previous: (projectId: string, issueId: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectId}/issues/${issueId}/previous`,
        null,
      ),
    uploadAttachment: (issueId: string, file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      return this.http.post<any>(`${this.baseUrl}/attachments/issue/${issueId}`, formData);
    },
  };

  readonly attachments = {
    generateUploadUrl: (fileName: string) =>
      this.http.post<{ uploadUrl: string; blobFileName: string }>(
        `${this.baseUrl}/attachments/generate-upload-url`,
        null,
        { params: { fileName } },
      ),
    uploadToAzure: (uploadUrl: string, file: File) =>
      this.http.put(uploadUrl, file, {
        headers: {
          'x-ms-blob-type': 'BlockBlob',
          'Content-Type': 'application/octet-stream',
        },
      }),
  };

  readonly reports = {
    getMonthlyReport: (projectId: string) =>
      this.http.get<any>(`${this.baseUrl}/reports/${projectId}`),
  };
}
