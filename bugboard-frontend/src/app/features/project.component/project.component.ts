import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Page } from '../../core/page.model';
import { ProjectResponse } from '../../core/project.model';
import { AuthService } from '../../core/auth/auth-service';
import { ProjectService } from '../../core/services/project.service';
import { NotificationService } from '../../core/services/notification.service';
import { ConfirmationModalService } from '../../core/services/confirmation-modal.service';
import { ConfirmationModalComponent } from '../../shared/components/confirmation-modal/confirmation-modal.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-project',
  standalone: true,
  imports: [CommonModule, RouterModule, PaginationComponent, ConfirmationModalComponent],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss',
})
export class ProjectComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly projectService = inject(ProjectService);
  private readonly notificationService = inject(NotificationService);
  private readonly confirmService = inject(ConfirmationModalService);
  private readonly pageSize: number = 9;

  // Pagination state
  public readonly currentPage = signal(0);
  public readonly pageSizeSignal = signal(this.pageSize);

  // API response
  public readonly projects = signal<Page<ProjectResponse> | null>(null);

  // Derived values
  public readonly projectList = computed(() => this.projects()?.content ?? []);
  public readonly totalPages = computed(() => this.projects()?.totalPages ?? 0);
  public readonly totalElements = computed(() => this.projects()?.totalElements ?? 0);

  // UI state
  public readonly loading = signal(true);
  public readonly error = signal<string | null>(null);

  public readonly isAdmin = computed(() => {
    const user = this.authService.currentUser();
    return user?.role === 'ADMIN';
  });

  public readonly isReadonly = this.authService.isReadonly;

  ngOnInit(): void {
    this.loadProjects();
  }

  public onPageChange(newPage: number): void {
    this.currentPage.set(newPage);
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

    this.projectService.getAll(this.currentPage(), this.pageSizeSignal()).subscribe({
      next: (page) => {
        this.projects.set(page);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('There was an error loading the projects. Try refreshing the page');
        this.loading.set(false);
      },
    });
  }

  public deleteProject(projectKey: string, event?: Event): void {
    // Prevent click propagation to parent routerLink
    if (event) {
      event.stopPropagation();
    }

    this.confirmService.open({
      title: 'Delete Project',
      message:
        'Are you sure you want to delete this project? This action cannot be undone. All associated issues and tags will be lost.',
      confirmButtonText: 'Delete Project',
      cancelButtonText: 'Keep Project',
      isDangerous: true,
      onConfirm: () => this.performDelete(projectKey),
    });
  }

  private performDelete(projectKey: string): void {
    // Optimistic update: remove from UI immediately
    const currentProjects = this.projects();
    if (currentProjects) {
      const updatedContent = currentProjects.content.filter((p) => p.key !== projectKey);
      this.projects.set({
        ...currentProjects,
        content: updatedContent,
        totalElements: currentProjects.totalElements - 1,
      });
    }

    this.projectService.delete(projectKey).subscribe({
      next: () => {
        this.notificationService.showSuccess(
          'Project Deleted',
          'The project has been successfully deleted.',
        );
        this.loadProjects();
      },
      error: () => {
        this.error.set('There was an error deleting the project. Please try again.');
      },
    });
  }
}
