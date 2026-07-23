import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../page.model';
import { ProjectResponse } from '../project.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly api = inject(ApiService);

  public getAll(page = 0, size = 12): Observable<Page<ProjectResponse>> {
    return this.api.projects.getAll(page, size);
  }

  public getById(id: string): Observable<ProjectResponse> {
    return this.api.projects.getById(id);
  }

  public create(name: string, description: string): Observable<ProjectResponse> {
    return this.api.projects.create(name, description);
  }

  public delete(id: string): Observable<void> {
    return this.api.projects.delete(id);
  }
}
