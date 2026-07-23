import { Component, inject, OnInit, signal, computed, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { IssueService } from '../../core/services/issue.service';
import { IssueResponse } from '../../core/issue.model';
import { Page } from '../../core/page.model';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-issue',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PaginationComponent],
  templateUrl: './issue.component.html',
})
export class IssueComponent implements OnInit {
  private readonly issueService = inject(IssueService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // Paginazione
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);

  @Input() projectId!: string;

  // Dati paginati dal backend
  issuesPage = signal<Page<IssueResponse> | null>(null);

  // Computed
  issues = computed(() => this.issuesPage()?.content ?? []);
  totalPages = computed(() => this.issuesPage()?.totalPages ?? 0);
  totalElements = computed(() => this.issuesPage()?.totalElements ?? 0);

  // Filtri e ordinamento
  statusFilter = signal<string>('ALL');
  priorityFilter = signal<string>('ALL');
  isLoading = signal<boolean>(false);
  sortField = signal<string>('title');
  sortDirection = signal<'asc' | 'desc'>('asc');

  ngOnInit(): void {
    if (!this.projectId) {
      this.projectId = this.route.snapshot.paramMap.get('projectId') || '';
    }

    if (this.projectId) {
      this.loadIssues();
    }
  }

  loadIssues(): void {
    this.isLoading.set(true);
    const page = this.currentPage();
    const size = this.pageSize();

    this.issueService
      .getIssuesByProject(
        this.projectId,
        this.statusFilter(),
        this.priorityFilter(),
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
    this.router.navigate(['/projects', this.projectId, 'issues', issue.id]);
  }

  getPriorityStyle(priority: string): string {
    if (!priority) return 'priority-lowest';
    return `priority-${priority.toLowerCase()}`;
  }

  getStatusLabel(status: string): string {
    return status ? status.replace(/_/g, ' ').toUpperCase() : '';
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

  // USER ACTIONS
  onActionClick(event: Event, issueId: string): void {
    event.stopPropagation();
    console.log('Action clicked for:', issueId);
  }

  editIssue(id: string): void {
    console.log('Edit issue:', id);
  }

  deleteIssue(id: string): void {
    console.log('Delete issue:', id);
  }
}
