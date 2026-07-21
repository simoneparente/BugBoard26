import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

import { CreateIssueDto } from '../models/create-issue.dto';

@Injectable({
  providedIn: 'root',
})
export class IssueService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  public createIssue(projectId: string, issue: CreateIssueDto): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/projects/${projectId}/issues`, issue);
  }

  public uploadAttachment(issueId: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.API_URL}/attachments/issue/${issueId}`, formData);
  }

  getIssuesByProject(projectId: string, page: number = 0, size: number = 10): Observable<SpringPage<IssueResponse>> {
    const url = `${this.projectsApiUrl}/${projectId}/issues`;
    
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    return this.http.get<SpringPage<IssueResponse>>(url, { params });
  }
}
