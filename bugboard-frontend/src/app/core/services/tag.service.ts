import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TagResponse } from '../tag.model';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiBaseUrl;

  public getTagsByProjectId(projectId: string): Observable<TagResponse[]> {
    return this.http.get<TagResponse[]>(`${this.API_URL}/tags/project/${projectId}`);
  }
}
