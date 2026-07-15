import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectResponse } from '../../core/project.model';
import { AuthService } from '../../core/auth/auth-service';
import { ProjectService } from '../../core/services/project.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-project',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss',
})
export class ProjectComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly projectService = inject(ProjectService);

  public readonly projects = signal<ProjectResponse[]>([]);
  public readonly loading = signal(true);
  public readonly error = signal<string | null>(null);

  public readonly isAdmin = computed(() => {
    const user = this.authService.currentUser();
    return user?.role === 'ADMIN';
  });

  ngOnInit(): void {
    this.loadProjects();
  }

  public getLastModifiedDate(project: ProjectResponse): string {
    if (!project.updatedAt) return '';
    return new Date(project.updatedAt).toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  public getIssueCount(project: ProjectResponse): number {
    return project.issues?.length ?? 0;
  }

  private loadProjects(): void {
    this.loading.set(true);
    this.error.set(null);

    this.projectService.getAll().subscribe({
      next: (page) => {
        this.projects.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load projects.');
        this.loading.set(false);
      },
    });
  }
}
