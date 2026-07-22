import {
  Component,
  OnInit,
  signal,
  computed,
  inject,
  PLATFORM_ID,
  ChangeDetectorRef,
  ElementRef,
  HostListener,
} from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { IssueService } from '../../core/services/issue.service';
import { NotificationService } from '../../core/services/notification.service';
import { IssueResponse } from '../../core/issue.model';
import { UserResponse } from '../../core/auth/auth.models';
import { BreadcrumbService } from '../../core/services/breadcrumb.service';
import { AuthService } from '../../core/auth/auth-service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-issue-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './issue-detail.component.html',
  styleUrls: ['./issue-detail.component.scss'],
})
export class IssueDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly issueService = inject(IssueService);
  private readonly notificationService = inject(NotificationService);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly elementRef = inject(ElementRef);

  public readonly issue = signal<IssueResponse | null>(null);
  public readonly users = signal<UserResponse[]>([]);
  public readonly isLoading = signal<boolean>(true);
  public readonly error = signal<string | null>(null);
  public readonly isUpdatingAssignee = signal<boolean>(false);
  public readonly isUpdatingStatus = signal<boolean>(false);
  public readonly isUploadingFile = signal<boolean>(false);

  public isUserDropdownOpen: boolean = false;
  public userSearchQuery: string = '';

  public readonly isAdmin = computed(() => {
    return this.authService.userRole() === 'ADMIN';
  });

  public projectId: string = '';
  public issueId: string = '';
  public selectedAssigneeId: string = '';

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.route.paramMap.subscribe((params) => {
        this.projectId = params.get('projectId') || '';
        this.issueId = params.get('issueId') || '';

        if (this.projectId && this.issueId) {
          this.loadData();
        } else {
          this.isLoading.set(false);
          this.error.set('Missing project or issue ID.');
        }
      });
    } else {
      this.isLoading.set(false);
    }
  }

  private loadData(): void {
    this.isLoading.set(true);
    this.error.set(null);

    // Load users list first or concurrently
    this.issueService.getAllUsers().subscribe({
      next: (usersList) => {
        this.users.set(usersList || []);
        this.fetchIssue();
      },
      error: (err) => {
        console.error('Failed to load users:', err);
        // Continue loading issue even if user list fails
        this.fetchIssue();
      },
    });
  }

  private fetchIssue(): void {
    this.issueService.getIssueById(this.projectId, this.issueId).subscribe({
      next: (data) => {
        this.issue.set(data);
        if (data.projectName) {
          this.breadcrumbService.setProjectName(data.projectName);
        }
        this.syncSelectedAssignee(data);
        this.isLoading.set(false);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching issue detail:', err);
        this.isLoading.set(false);
        if (err.status === 404) {
          this.error.set('Issue not found.');
        } else {
          this.error.set('Failed to load issue details. Please try again later.');
        }
        this.cdr.detectChanges();
      },
    });
  }

  private syncSelectedAssignee(currentIssue: IssueResponse): void {
    if (!currentIssue.assigneeUsername || currentIssue.assigneeUsername === 'Unassigned') {
      this.selectedAssigneeId = '';
    } else {
      const match = this.users().find((u) => u.username === currentIssue.assigneeUsername);
      this.selectedAssigneeId = match ? match.id : '';
    }
  }

  public onRemoveAssignee(): void {
    if (this.issue()?.status === 'COMPLETED') {
      this.notificationService.showError(
        'Action Not Allowed',
        'Cannot remove assignee from a COMPLETED issue.',
      );
      return;
    }
    if (this.issue()?.status === 'CLOSED') {
      this.notificationService.showError(
        'Action Not Allowed',
        'Cannot change assignee on a CLOSED issue.',
      );
      return;
    }
    this.isUpdatingAssignee.set(true);
    this.issueService.removeAssignee(this.projectId, this.issueId).subscribe({
      next: (updatedIssue) => {
        this.issue.set(updatedIssue);
        this.syncSelectedAssignee(updatedIssue);
        this.isUpdatingAssignee.set(false);
        this.notificationService.showSuccess(
          'Assignee Removed',
          'The issue has been reset to TO_DO status with no assignee.',
        );
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error removing assignee:', err);
        this.isUpdatingAssignee.set(false);
        this.notificationService.showError(
          'Update Failed',
          err.error?.message || 'Failed to unassign the issue. Please try again.',
        );
        this.syncSelectedAssignee(this.issue()!);
        this.cdr.detectChanges();
      },
    });
  }

  public get filteredUsers(): UserResponse[] {
    const q = (this.userSearchQuery || '').trim().toLowerCase();
    return (this.users() || []).filter(
      (u) =>
        q === '' ||
        u.username.toLowerCase().includes(q) ||
        (u.role && u.role.toLowerCase().includes(q)),
    );
  }

  @HostListener('document:click', ['$event'])
  public onDocumentClick(event: MouseEvent): void {
    const clickedInside = this.elementRef.nativeElement
      .querySelector('.assignee-search-wrapper')
      ?.contains(event.target);
    if (!clickedInside) {
      this.isUserDropdownOpen = false;
    }
  }

  public selectUserAssignee(user: UserResponse): void {
    this.isUserDropdownOpen = false;
    this.userSearchQuery = '';
    this.isUpdatingAssignee.set(true);
    this.issueService.assignIssue(this.projectId, this.issueId, user.id).subscribe({
      next: (updatedIssue) => {
        this.issue.set(updatedIssue);
        this.syncSelectedAssignee(updatedIssue);
        this.isUpdatingAssignee.set(false);
        this.notificationService.showSuccess(
          'Assignee Updated',
          `Issue assigned to ${user.username}.`,
        );
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error assigning issue:', err);
        this.isUpdatingAssignee.set(false);
        this.notificationService.showError(
          'Update Failed',
          err.error?.message || 'Failed to assign user to issue.',
        );
        this.syncSelectedAssignee(this.issue()!);
        this.cdr.detectChanges();
      },
    });
  }

  public onAssigneeChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const newUserId = target.value;

    this.isUpdatingAssignee.set(true);

    if (!newUserId) {
      // Remove assignee
      this.issueService.removeAssignee(this.projectId, this.issueId).subscribe({
        next: (updatedIssue) => {
          this.issue.set(updatedIssue);
          this.syncSelectedAssignee(updatedIssue);
          this.isUpdatingAssignee.set(false);
          this.notificationService.showSuccess('Assignee Removed', 'The issue is now unassigned.');
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error removing assignee:', err);
          this.isUpdatingAssignee.set(false);
          this.notificationService.showError(
            'Update Failed',
            'Failed to unassign the issue. Please try again.',
          );
          this.syncSelectedAssignee(this.issue()!);
          this.cdr.detectChanges();
        },
      });
    } else {
      // Assign issue to user
      this.issueService.assignIssue(this.projectId, this.issueId, newUserId).subscribe({
        next: (updatedIssue) => {
          this.issue.set(updatedIssue);
          this.syncSelectedAssignee(updatedIssue);
          this.isUpdatingAssignee.set(false);
          const assignedUser = this.users().find((u) => u.id === newUserId);
          this.notificationService.showSuccess(
            'Assignee Updated',
            `Issue assigned to ${assignedUser ? assignedUser.username : 'selected user'}.`,
          );
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error assigning issue:', err);
          this.isUpdatingAssignee.set(false);
          this.notificationService.showError(
            'Update Failed',
            'Failed to assign user to issue. Please try again.',
          );
          this.syncSelectedAssignee(this.issue()!);
          this.cdr.detectChanges();
        },
      });
    }
  }

  public onUploadFileSelected(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      const file = files[0];
      this.isUploadingFile.set(true);
      this.issueService.uploadAttachment(this.issueId, file).subscribe({
        next: () => {
          this.isUploadingFile.set(false);
          this.notificationService.showSuccess(
            'Attachment Uploaded',
            'File uploaded successfully!',
          );
          this.fetchIssue();
        },
        error: (err) => {
          console.error('Error uploading attachment:', err);
          this.isUploadingFile.set(false);
          this.notificationService.showError('Upload Error', 'Failed to upload attachment.');
          this.cdr.detectChanges();
        },
      });
    }
  }

  public onStatusSelectChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const newStatus = target.value;

    if (!newStatus || newStatus === this.issue()?.status) return;

    this.onSetStatus(newStatus);
  }

  public onSetStatus(status: string): void {
    if (!status || status === this.issue()?.status) return;

    this.updateStatusCall(
      this.issueService.setStatus(this.projectId, this.issueId, status),
      'Status Updated',
      `Issue status changed to ${status}`,
    );
  }

  public onStartProgress(): void {
    this.updateStatusCall(
      this.issueService.startProgress(this.projectId, this.issueId),
      'Issue started',
      'Status updated to In Progress',
    );
  }

  public onAccept(): void {
    this.updateStatusCall(
      this.issueService.acceptIssue(this.projectId, this.issueId),
      'Marked for Review',
      'Issue has been marked for review',
    );
  }

  public onApprove(): void {
    this.onSetStatus('COMPLETED');
  }

  public onRejectNotFixed(): void {
    this.onSetStatus('NOT_FIXED');
  }

  public onCloseIssue(): void {
    this.onSetStatus('CLOSED');
  }

  public onRollback(): void {
    this.updateStatusCall(
      this.issueService.rollbackStatus(this.projectId, this.issueId),
      'Status rolled back',
      'Issue reverted to previous status',
    );
  }

  private updateStatusCall(obs: any, successTitle: string, successMsg: string): void {
    this.isUpdatingStatus.set(true);
    obs.subscribe({
      next: (updatedIssue: IssueResponse) => {
        this.issue.set(updatedIssue);
        this.isUpdatingStatus.set(false);
        this.notificationService.showSuccess(successTitle, successMsg);
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error updating status:', err);
        this.isUpdatingStatus.set(false);
        const backendMessage = err?.error?.message || err?.message || '';
        if (backendMessage.toLowerCase().includes('unassigned')) {
          this.notificationService.showError(
            'Cannot Start Progress',
            'Please assign a user to the issue before starting progress.',
          );
        } else {
          this.notificationService.showError(
            'Status Error',
            backendMessage || 'Failed to change issue status.',
          );
        }
        this.cdr.detectChanges();
      },
    });
  }

  public getStatusBadgeClass(status?: string): string {
    switch (status?.toUpperCase()) {
      case 'TO_DO':
      case 'NEW':
      case 'OPEN':
        return 'status-badge status-to-do';
      case 'IN_PROGRESS':
        return 'status-badge status-in-progress';
      case 'MARKED_FOR_REVIEW':
        return 'status-badge status-marked-for-review';
      case 'NOT_FIXED':
        return 'status-badge status-not-fixed';
      case 'COMPLETED':
      case 'RESOLVED':
      case 'ACCEPTED':
        return 'status-badge status-completed';
      case 'CLOSED':
        return 'status-badge status-closed';
      default:
        return 'status-badge status-to-do';
    }
  }

  public getPriorityBadgeClass(priority?: string): string {
    switch (priority?.toUpperCase()) {
      case 'LOWEST':
        return 'priority-badge priority-lowest';
      case 'LOW':
        return 'priority-badge priority-low';
      case 'MEDIUM':
        return 'priority-badge priority-medium';
      case 'HIGH':
        return 'priority-badge priority-high';
      case 'HIGHEST':
      case 'CRITICAL':
        return 'priority-badge priority-highest';
      default:
        return 'priority-badge priority-medium';
    }
  }

  public getTypeBadgeClass(type?: string): string {
    switch (type?.toUpperCase()) {
      case 'BUG':
        return 'bg-danger-subtle text-danger border border-danger-subtle';
      case 'FEATURE':
        return 'bg-primary-subtle text-primary border border-primary-subtle';
      case 'ENHANCEMENT':
        return 'bg-success-subtle text-success border border-success-subtle';
      default:
        return 'bg-light text-dark border';
    }
  }

  public formatDate(dateStr?: string): string {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  public formatFileSize(bytes?: number): string {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  public getAttachmentDownloadUrl(attachmentId: string): string {
    return `${environment.apiBaseUrl}/attachments/${attachmentId}/download`;
  }

  public getAttachmentViewUrl(attachmentId: string): string {
    return `${environment.apiBaseUrl}/attachments/${attachmentId}/view`;
  }

  public isImageAttachment(fileName?: string): boolean {
    if (!fileName) return false;
    const lower = fileName.toLowerCase();
    return (
      lower.endsWith('.png') ||
      lower.endsWith('.jpg') ||
      lower.endsWith('.jpeg') ||
      lower.endsWith('.gif') ||
      lower.endsWith('.svg') ||
      lower.endsWith('.webp')
    );
  }
}
