import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService } from '../../core/services/project.service';
import { ProjectResponse } from '../../core/project.model';
import { NotificationService } from '../../core/services/notification.service';
import { ProjectMembersListComponent } from '../../shared/components/project-members-list/project-members-list.component';

@Component({
  selector: 'app-project-settings',
  standalone: true,
  imports: [CommonModule, ProjectMembersListComponent],
  templateUrl: './project-settings.component.html',
  styleUrl: './project-settings.component.scss',
})
export class ProjectSettingsComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly notificationService = inject(NotificationService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // State signals
  project = signal<ProjectResponse | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadProject();
  }

  private loadProject(): void {
    const projectId = this.activatedRoute.snapshot.paramMap.get('projectId');

    if (!projectId) {
      this.router.navigate(['/projects']);
      return;
    }

    this.projectService.getById(projectId).subscribe({
      next: (project: ProjectResponse) => {
        this.project.set(project);
        this.isLoading.set(false);
      },
      error: (err: any) => {
        this.isLoading.set(false);
        this.error.set('Failed to load project settings');
        this.notificationService.showError('Error', 'Could not load project');
        this.router.navigate(['/projects']);
      },
    });
  }

  public deleteProject(projectId: string): void {
    if (!confirm('Are you sure you want to delete this project? This action cannot be undone.')) {
      return;
    }
    this.projectService.delete(projectId).subscribe({
      next: () => {
        this.notificationService.showSuccess(
          'Project Deleted',
          'The project has been successfully deleted.',
        );
        this.router.navigate(['/projects']);
      },
      error: () => {
        this.error.set('There was an error deleting the project. Please try again.');
      },
    });
  }

  goBack(): void {
    const projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.router.navigate(['/projects', projectId, 'issues']);
  }
}
