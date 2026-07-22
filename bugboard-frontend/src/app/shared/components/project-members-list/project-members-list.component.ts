import { Component, Input, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectService } from '../../../core/services/project.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AddProjectMembersComponent } from '../add-project-members/add-project-members.component';
import { UserResponse } from '../../../core/auth/auth.models';
import { Page } from '../../../core/page.model';

@Component({
  selector: 'app-project-members-list',
  standalone: true,
  imports: [CommonModule, AddProjectMembersComponent],
  templateUrl: './project-members-list.component.html',
  styleUrl: './project-members-list.component.scss',
})
export class ProjectMembersListComponent implements OnInit {
  @Input() projectId!: string;

  private readonly projectService = inject(ProjectService);
  private readonly notificationService = inject(NotificationService);

  // State signals
  members = signal<UserResponse[]>([]);
  isLoading = signal(true);
  error = signal<string | null>(null);
  currentPage = signal(0);
  totalPages = signal(0);
  totalMembers = signal(0);
  pageSize = 10;

  ngOnInit(): void {
    this.loadMembers();
  }

  private loadMembers(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.projectService
      .getProjectMembers(this.projectId, this.currentPage(), this.pageSize)
      .subscribe({
        next: (response: Page<UserResponse>) => {
          this.members.set(response.content ?? []);
          this.totalPages.set(response.totalPages);
          this.totalMembers.set(response.totalElements);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.error.set('Failed to load project members');
          this.notificationService.showError('Error', 'Could not load members');
        },
      });
  }

  onMembersAdded(addedMembers: UserResponse[]): void {
    this.notificationService.showSuccess('Success', `Added ${addedMembers.length} member(s)`);
    this.loadMembers();
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadMembers();
    }
  }

  getRoleColor(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'badge bg-danger';
      case 'TECHNICAL':
        return 'badge bg-primary';
      case 'EXTERNAL':
        return 'badge bg-secondary';
      default:
        return 'badge bg-light text-dark';
    }
  }
}
