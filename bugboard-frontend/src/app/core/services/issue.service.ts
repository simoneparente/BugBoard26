import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IssueResponse } from '../issue.model';
import { environment } from '../../../environments/environment';
import { SpringPage } from '../spring-page.model';

@Injectable({ providedIn: 'root' })
export class IssueService {
  private http = inject(HttpClient);
  private projectsApiUrl = environment.projectsApiUrl;

  getIssuesByProject(projectId: string, page: number = 0, size: number = 10): Observable<SpringPage<IssueResponse>> {
    const url = `${this.projectsApiUrl}/${projectId}/issues`;
    
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    return this.http.get<SpringPage<IssueResponse>>(url, { params });
  }
}