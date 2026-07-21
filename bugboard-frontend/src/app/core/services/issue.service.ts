import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { IssueRequest, IssueResponse } from '../issue.model';
import { ApiService } from './api.service';
import { Page } from '../page.model';
import { IssueResponse } from '../issue.model';

@Injectable({
  providedIn: 'root',
})
export class IssueService {
  private readonly apiService = inject(ApiService);

  public createIssue(projectId: string, issue: IssueRequest): Observable<IssueResponse> {
    return this.apiService.issues.create(projectId, issue);
  }

  public uploadAttachment(issueId: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.apiService.issues.uploadAttachment(issueId, file);
  }

  getIssuesByProject(
    projectId: string,
    page: number = 0,
    size: number = 20,
  ): Observable<Page<IssueResponse>> {
    const url = `${this.API_URL}/projects/${projectId}/issues`;

    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    return this.http.get<Page<IssueResponse>>(url, { params });
  }
}
