import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { IssueRequest, IssueResponse } from '../issue.model';
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
    search: string = '',
    page: number = 0,
    size: number = 20,
    sortField: string = 'id',
    sortDirection: string = 'desc',
  ): Observable<Page<IssueResponse>> {
    return this.apiService.issues.getByProject(
      projectId,
      status,
      priority,
      search,
      page,
      size,
      sortField,
      sortDirection,
    );
  }

  public createIssue(projectId: string, issue: IssueRequest): Observable<IssueResponse> {
    return this.apiService.issues.create(projectId, issue);
  }

  public uploadAttachment(issueId: string, file: File): Observable<any> {
    return this.apiService.issues.uploadAttachment(issueId, file);
  }
}
