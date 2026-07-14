import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProjectResponse } from '../project.model';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/projects`;

  public getAll(page = 0, size = 20): Observable<PageResponse<ProjectResponse>> {
    return this.http.get<PageResponse<ProjectResponse>>(this.API_URL, {
      params: { page: page.toString(), size: size.toString() },
    });
  }

  public getById(id: string): Observable<ProjectResponse> {
    return this.http.get<ProjectResponse>(`${this.API_URL}/${id}`);
  }

  public create(name: string, description: string): Observable<ProjectResponse> {
    return this.http.post<ProjectResponse>(this.API_URL, { name, description });
  }
}
