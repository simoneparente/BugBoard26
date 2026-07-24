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
    projectKey: string,
    status: string = 'ALL',
    priority: string = 'ALL',
    search: string = '',
    page: number = 0,
    size: number = 20,
    sortField: string = 'id',
    sortDirection: string = 'desc',
  ): Observable<Page<IssueResponse>> {
    return this.apiService.issues.getByProject(
      projectKey,
      status,
      priority,
      search,
      page,
      size,
      sortField,
      sortDirection,
    );
  }

  public createIssue(projectKey: string, issue: IssueRequest): Observable<IssueResponse> {
    return this.apiService.issues.create(projectKey, issue);
  }

  public updateIssue(
    projectKey: string,
    sequenceNumber: string,
    issue: IssueRequest,
  ): Observable<IssueResponse> {
    return this.apiService.issues.update(projectKey, sequenceNumber, issue);
  }

  public getIssueById(projectKey: string, sequenceNumber: string): Observable<IssueResponse> {
    return this.apiService.issues.getById(projectKey, sequenceNumber);
  }

  public assignIssue(
    projectKey: string,
    sequenceNumber: string,
    assigneeId: string,
  ): Observable<IssueResponse> {
    return this.apiService.issues.assign(projectKey, sequenceNumber, assigneeId);
  }

  public removeAssignee(projectKey: string, sequenceNumber: string): Observable<IssueResponse> {
    return this.apiService.issues.removeAssignee(projectKey, sequenceNumber);
  }

  public setStatus(
    projectKey: string,
    sequenceNumber: string,
    status: string,
  ): Observable<IssueResponse> {
    return this.apiService.issues.setStatus(projectKey, sequenceNumber, status);
  }

  public startProgress(projectKey: string, sequenceNumber: string): Observable<IssueResponse> {
    return this.apiService.issues.startProgress(projectKey, sequenceNumber);
  }

  public acceptIssue(projectKey: string, sequenceNumber: string): Observable<IssueResponse> {
    return this.apiService.issues.accept(projectKey, sequenceNumber);
  }

  public rollbackStatus(projectKey: string, sequenceNumber: string): Observable<IssueResponse> {
    return this.apiService.issues.previous(projectKey, sequenceNumber);
  }

  public deleteIssue(projectKey: string, sequenceNumber: string): Observable<void> {
    return this.apiService.issues.delete(projectKey, sequenceNumber);
  }

  public exportIssues(projectKey: string, format: string): Observable<Blob> {
    return this.apiService.issues.export(projectKey, format);
  }

  public getAllUsers(): Observable<UserResponse[]> {
    return this.apiService.users.getAll();
  }

  public uploadAttachment(sequenceNumber: string, file: File): Observable<any> {
    return this.apiService.issues.uploadAttachment(sequenceNumber, file);
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
