import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { IssueRequest, IssueResponse } from '../issue.model';
import { UserResponse } from '../auth/auth.models';
import { ApiService } from './api.service';
import { Page } from '../page.model';

@Injectable({
  providedIn: 'root',
})
export class IssueService {
  private readonly apiService = inject(ApiService);

  public getIssuesByProject(
    projectId: string,
    status: string = 'ALL',
    priority: string = 'ALL',
    page: number = 0,
    size: number = 20,
    sortField: string = 'id',
    sortDirection: string = 'desc',
  ): Observable<Page<IssueResponse>> {
    return this.apiService.issues.getByProject(
      projectId,
      status,
      priority,
      page,
      size,
      sortField,
      sortDirection,
    );
  }

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

  public deleteIssue(projectId: string, issueId: string): Observable<void> {
    return this.apiService.issues.delete(projectId, issueId);
  }

  public getAllUsers(): Observable<UserResponse[]> {
    return this.apiService.users.getAll();
  }

  public uploadAttachment(issueId: string, file: File): Observable<any> {
    return this.apiService.issues.uploadAttachment(issueId, file);
  }

  public generateUploadUrl(
    fileName: string,
  ): Observable<{ uploadUrl: string; blobFileName: string }> {
    return this.apiService.attachments.generateUploadUrl(fileName);
  }

  public uploadToAzure(uploadUrl: string, file: File): Observable<any> {
    return this.apiService.attachments.uploadToAzure(uploadUrl, file);
  }
}
