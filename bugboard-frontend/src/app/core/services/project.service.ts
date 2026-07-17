import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../page.model';
import { ProjectResponse } from '../project.model';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/projects`;

  public getAll(page = 0, size = 12): Observable<Page<ProjectResponse>> {
    return this.http.get<Page<ProjectResponse>>(this.API_URL, {
      params: { page: page.toString(), size: size.toString() },
    });
  }

  public getById(id: string): Observable<ProjectResponse> {
    return this.http.get<ProjectResponse>(`${this.API_URL}/${id}`);
  }

  public create(name: string, description: string): Observable<ProjectResponse> {
    return this.http.post<ProjectResponse>(this.API_URL, { name, description });
  }

  public delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
