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
import { AssigneeRecommendation } from '../assignee-recommendation.model';

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
    getRecommendedAssignees: (id: string) =>
      this.http.get<AssigneeRecommendation[]>(
        `${this.baseUrl}/projects/${id}/recommended-assignees`,
      ),
    create: (name: string, description: string) =>
      this.http.post<ProjectResponse>(`${this.baseUrl}/projects`, { name, description }),
    delete: (id: string) => this.http.delete<void>(`${this.baseUrl}/projects/${id}`),
  };

  readonly issues = {
    getByProject: (
      projectKey: string,
      status: string = 'ALL',
      priority: string = 'ALL',
      search: string = '',
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
      if (search && search.trim() !== '') {
        params = params.set('search', search.trim());
      }

      return this.http.get<Page<IssueResponse>>(`${this.baseUrl}/projects/${projectKey}/issues`, {
        params,
      });
    },
    create: (projectKey: string, payload: any) =>
      this.http.post<IssueResponse>(`${this.baseUrl}/projects/${projectKey}/issues`, payload),
    getById: (projectKey: string, sequenceNumber: string) =>
      this.http.get<IssueResponse>(
        `${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}`,
      ),

    assign: (projectKey: string, sequenceNumber: string, assigneeId: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}/assign`,
        null,
        { params: { assigneeId } },
      ),
    removeAssignee: (projectKey: string, sequenceNumber: string) =>
      this.http.delete<IssueResponse>(
        `${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}/assignee`,
      ),
    setStatus: (projectKey: string, sequenceNumber: string, status: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}/status`,
        null,
        { params: { status } },
      ),
    startProgress: (projectKey: string, sequenceNumber: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}/start-progress`,
        null,
      ),
    accept: (projectKey: string, sequenceNumber: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}/accept`,
        null,
      ),
    previous: (projectKey: string, sequenceNumber: string) =>
      this.http.put<IssueResponse>(
        `${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}/previous`,
        null,
      ),
    delete: (projectKey: string, sequenceNumber: string) =>
      this.http.delete<void>(`${this.baseUrl}/projects/${projectKey}/issues/${sequenceNumber}`),
    export: (projectKey: string, format: string) =>
      this.http.get(`${this.baseUrl}/projects/${projectKey}/issues/export`, {
        params: { format },
        responseType: 'blob',
      }),
    uploadAttachment: (sequenceNumber: string, file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      return this.http.post<any>(`${this.baseUrl}/attachments/issue/${sequenceNumber}`, formData);
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
          'Content-Type': file.type || 'application/octet-stream',
        },
      }),
  };

  readonly reports = {
    getMonthlyReport: (projectKey: string) =>
      this.http.get<any>(`${this.baseUrl}/reports/${projectKey}`),
  };

  readonly projectMembers = {
    getMembers: (projectKey: string, page: number, size: number) =>
      this.http.get<Page<UserResponse>>(`${this.baseUrl}/projects/${projectKey}/members`, {
        params: { page: page.toString(), size: size.toString() },
      }),
    getAvailable: (projectKey: string, page: number, size: number) =>
      this.http.get<Page<UserResponse>>(`${this.baseUrl}/projects/${projectKey}/available-users`, {
        params: { page: page.toString(), size: size.toString() },
      }),
    addMembers: (projectKey: string, userIds: string[]) =>
      this.http.post<UserResponse[]>(`${this.baseUrl}/projects/${projectKey}/members`, {
        userIds,
      }),
  };
}
