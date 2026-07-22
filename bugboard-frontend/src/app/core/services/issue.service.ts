import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { IssueRequest, IssueResponse } from '../issue.model';
import { UserResponse } from '../auth/auth.models';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root',
})
export class IssueService {
  private readonly apiService = inject(ApiService);

  public createIssue(projectId: string, issue: IssueRequest): Observable<IssueResponse> {
    return this.apiService.issues.create(projectId, issue);
  }

  public getIssueById(projectId: string, issueId: string): Observable<IssueResponse> {
    return this.apiService.issues.getById(projectId, issueId);
  }

  public assignIssue(
    projectId: string,
    issueId: string,
    assigneeId: string,
  ): Observable<IssueResponse> {
    return this.apiService.issues.assign(projectId, issueId, assigneeId);
  }

  public removeAssignee(projectId: string, issueId: string): Observable<IssueResponse> {
    return this.apiService.issues.removeAssignee(projectId, issueId);
  }

  public setStatus(projectId: string, issueId: string, status: string): Observable<IssueResponse> {
    return this.apiService.issues.setStatus(projectId, issueId, status);
  }

  public startProgress(projectId: string, issueId: string): Observable<IssueResponse> {
    return this.apiService.issues.startProgress(projectId, issueId);
  }

  public acceptIssue(projectId: string, issueId: string): Observable<IssueResponse> {
    return this.apiService.issues.accept(projectId, issueId);
  }

  public rollbackStatus(projectId: string, issueId: string): Observable<IssueResponse> {
    return this.apiService.issues.previous(projectId, issueId);
  }

  public getAllUsers(): Observable<UserResponse[]> {
    return this.apiService.users.getAll();
  }

  public uploadAttachment(issueId: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.apiService.issues.uploadAttachment(issueId, file);
  }
}
