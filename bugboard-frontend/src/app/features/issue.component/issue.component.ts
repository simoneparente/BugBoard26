import { Component, inject, OnInit, OnDestroy, signal, computed, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { IssueService } from '../../core/services/issue.service';
import { ProjectService } from '../../core/services/project.service';
import { BreadcrumbService } from '../../core/services/breadcrumb.service';
import { IssueResponse } from '../../core/issue.model';
import { Page } from '../../core/page.model';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { AuthService } from '../../core/auth/auth-service';

@Component({
  selector: 'app-issue',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PaginationComponent],
  templateUrl: './issue.component.html',
})
export class IssueComponent implements OnInit, OnDestroy {
  private readonly issueService = inject(IssueService);
  private readonly projectService = inject(ProjectService);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly authService = inject(AuthService);

  /** True when the current user has EXTERNAL (read-only) role. Used in the template to hide write controls. */
  readonly isReadonly = this.authService.isReadonly;

  private readonly searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  // Paginazione
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);

  @Input() projectKey!: string;
  projectName = signal<string>('');

  // Dati paginati dal backend
  issuesPage = signal<Page<IssueResponse> | null>(null);

  // Computed
  issues = computed(() => this.issuesPage()?.content ?? []);
  totalPages = computed(() => this.issuesPage()?.totalPages ?? 0);
  totalElements = computed(() => this.issuesPage()?.totalElements ?? 0);

  // Filtri e ordinamento
  statusFilter = signal<string>('ALL');
  priorityFilter = signal<string>('ALL');
  searchQuery = signal<string>('');
  isLoading = signal<boolean>(false);
  sortField = signal<string>('title');
  sortDirection = signal<'asc' | 'desc'>('asc');

  ngOnInit(): void {
    if (!this.projectKey) {
      this.projectKey = (this.route.snapshot.paramMap.get('projectKey') || '').toUpperCase();
    }

    if (this.projectKey) {
      this.projectService.getById(this.projectKey).subscribe({
        next: (project) => {
          this.projectName.set(project.name);
          this.breadcrumbService.setProjectName(project.name);
        },
        error: (err) => console.error('Failed to load project details', err),
      });
      this.loadIssues();
    }

    this.searchSubscription = this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) => {
          this.isLoading.set(true);
          return this.issueService.getIssuesByProject(
            this.projectKey,
            this.statusFilter(),
            this.priorityFilter(),
            query,
            this.currentPage(),
            this.pageSize(),
            this.sortField() || 'title',
            this.sortDirection(),
          );
        }),
      )
      .subscribe({
        next: (response) => {
          this.issuesPage.set(response);
          this.isLoading.set(false);
        },
        error: (error) => {
          console.error('Failed to load issues', error);
          this.isLoading.set(false);
        },
      });
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  loadIssues(): void {
    this.isLoading.set(true);
    const page = this.currentPage();
    const size = this.pageSize();

    this.issueService
      .getIssuesByProject(
        this.projectKey,
        this.statusFilter(),
        this.priorityFilter(),
        this.searchQuery(),
        this.currentPage(),
        this.pageSize(),
        this.sortField() || 'title',
        this.sortDirection(),
      )
      .subscribe({
        next: (response) => {
          this.issuesPage.set(response);
          this.isLoading.set(false);
        },
        error: (error) => {
          console.error('Failed to load issues', error);
          this.isLoading.set(false);
        },
      });
  }

  // UI ACTIONS

  onSearchInput(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    this.searchSubject.next(query);
  }

  onSearchEnter(): void {
    this.currentPage.set(0);
    this.loadIssues();
  }

  clearSearch(): void {
    this.searchQuery.set('');
    this.currentPage.set(0);
    this.loadIssues();
  }

  onPageChange(newPage: number): void {
    this.currentPage.set(newPage);
    this.loadIssues();
  }

  onStatusChange(status: string): void {
    this.statusFilter.set(status);
    this.currentPage.set(0);
    this.loadIssues();
  }

  onPriorityChange(priority: string): void {
    this.priorityFilter.set(priority);
    this.currentPage.set(0);
    this.loadIssues();
  }

  sortData(field: string): void {
    if (field !== 'title' && field !== 'assignee') return;

    const targetField = field === 'assignee' ? 'assignee.username' : 'title';

    if (this.sortField() === targetField) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(targetField);
      this.sortDirection.set('asc');
    }

    this.currentPage.set(0);
    this.loadIssues();
  }

  onTitleClick(issue: IssueResponse): void {
    this.router.navigate(['/projects', this.projectKey, 'issues', issue.sequenceNumber]);
  }

  readonly statusOptions = [
    'TO_DO',
    'IN_PROGRESS',
    'MARKED_FOR_REVIEW',
    'NOT_FIXED',
    'COMPLETED',
    'CLOSED',
  ];

  readonly priorityOptions = ['HIGHEST', 'HIGH', 'MEDIUM', 'LOW', 'LOWEST'];

  getPriorityStyle(priority: string): string {
    if (!priority) return 'priority-lowest';
    return `priority-${priority.toLowerCase()}`;
  }

  getPriorityIcon(priority?: string): string {
    switch (priority?.toLowerCase()) {
      case 'highest':
        return 'bi-chevron-double-up';
      case 'high':
        return 'bi-chevron-up';
      case 'medium':
        return 'bi-dash-lg';
      case 'low':
        return 'bi-chevron-down';
      case 'lowest':
        return 'bi-chevron-double-down';
      default:
        return 'bi-sliders';
    }
  }

  getStatusLabel(status: string): string {
    return status ? status.replace(/_/g, ' ').toUpperCase() : '';
  }

  getStatusIcon(status?: string): string {
    switch (status?.toUpperCase()) {
      case 'TO_DO':
      case 'NEW':
      case 'OPEN':
        return 'bi-circle';
      case 'IN_PROGRESS':
        return 'bi-hourglass-split';
      case 'MARKED_FOR_REVIEW':
        return 'bi-eye-fill';
      case 'NOT_FIXED':
        return 'bi-x-circle-fill';
      case 'COMPLETED':
      case 'RESOLVED':
      case 'ACCEPTED':
        return 'bi-check-circle-fill';
      case 'CLOSED':
        return 'bi-archive-fill';
      default:
        return 'bi-funnel';
    }
  }

  getStatusBadgeClass(status?: string): string {
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

  getTagStyle(tagName: string): string {
    const styles: { [key: string]: string } = {
      Security: 'bg-danger-subtle text-danger border-danger',
      Mobile: 'bg-info-subtle text-info border-info',
      'UI/UX': 'bg-warning-subtle text-warning border-warning',
      Content: 'bg-secondary-subtle text-secondary border-secondary',
      Feature: 'bg-success-subtle text-success border-success',
    };
    return styles[tagName] || 'bg-light text-secondary border';
  }
}
