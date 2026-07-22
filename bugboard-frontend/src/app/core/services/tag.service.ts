import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TagRequest, TagResponse } from '../tag.model';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/tags`;

  /**
   * Retrieve all tags for a specific project.
   */
  public getTagsByProjectId(projectId: string): Observable<TagResponse[]> {
    return this.http.get<TagResponse[]>(`${this.API_URL}/project/${projectId}`);
  }

  /**
   * Retrieve a single tag by its ID.
   */
  public getTagById(id: string): Observable<TagResponse> {
    return this.http.get<TagResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * Create a new tag.
   */
  public createTag(request: TagRequest): Observable<TagResponse> {
    return this.http.post<TagResponse>(this.API_URL, request);
  }

  /**
   * Update an existing tag.
   */
  public updateTag(id: string, request: TagRequest): Observable<TagResponse> {
    return this.http.put<TagResponse>(`${this.API_URL}/${id}`, request);
  }

  /**
   * Delete a tag by its ID.
   */
  public deleteTag(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
