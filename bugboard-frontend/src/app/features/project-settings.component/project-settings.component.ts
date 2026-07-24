import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService } from '../../core/services/project.service';
import { ProjectResponse } from '../../core/project.model';
import { NotificationService } from '../../core/services/notification.service';
import { ConfirmationModalService } from '../../core/services/confirmation-modal.service';
import { ProjectMembersListComponent } from '../../shared/components/project-members-list/project-members-list.component';
import { ConfirmationModalComponent } from '../../shared/components/confirmation-modal/confirmation-modal.component';

@Component({
  selector: 'app-project-settings',
  standalone: true,
  imports: [CommonModule, ProjectMembersListComponent, ConfirmationModalComponent],
  templateUrl: './project-settings.component.html',
  styleUrl: './project-settings.component.scss',
})
export class ProjectSettingsComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly notificationService = inject(NotificationService);
  private readonly confirmationModalService = inject(ConfirmationModalService);
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
    const projectKey = this.activatedRoute.snapshot.paramMap.get('projectKey');

    if (!projectKey) {
      this.router.navigate(['/projects']);
      return;
    }

    this.projectService.getById(projectKey).subscribe({
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

  public deleteProject(projectKey: string, event?: Event): void {
    // Prevent click propagation to parent routerLink
    if (event) {
      event.stopPropagation();
    }

    this.confirmationModalService.open({
      title: 'Delete Project',
      message:
        'Are you sure you want to delete this project? This action cannot be undone. All associated issues and tags will be lost.',
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      isDangerous: true,
      onConfirm: () => this.performDelete(projectKey),
    });
  }

  private performDelete(projectKey: string): void {
    this.projectService.delete(projectKey).subscribe({
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
    const projectKey = this.activatedRoute.snapshot.paramMap.get('projectKey');
    this.router.navigate(['/projects', projectKey, 'issues']);
  }
}
