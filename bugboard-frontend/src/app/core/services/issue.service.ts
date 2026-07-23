import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { IssueRequest, IssueResponse } from '../issue.model';
import { ApiService } from './api.service';

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
}
