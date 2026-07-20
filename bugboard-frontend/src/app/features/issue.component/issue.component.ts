import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { IssueService } from '../../core/services/issue.service';
import { IssueResponse } from '../../core/issue.model';

@Component({
  selector: 'app-issue',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './issue.component.html'
})
export class IssueComponent implements OnInit {
  private issueService = inject(IssueService);
  private route = inject(ActivatedRoute);

  allIssues: IssueResponse[] = [];

  issues = signal<IssueResponse[]>([]);
  statusFilter = signal<string>('ALL');
  priorityFilter = signal<string>('ALL');
  isLoading = signal<boolean>(false);
  sortField = signal<string>('');
  sortDirection = signal<'asc' | 'desc'>('asc');

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('projectId');
    if (projectId) {
      this.loadIssues(projectId);
    }
  }

  loadIssues(projectId: string): void {
    this.isLoading.set(true);
    this.issueService.getIssuesByProject(projectId).subscribe({
      next: (response) => {
        console.log('Risposta integrale del backend:', response);
        
        this.allIssues = Array.isArray(response) ? response : (response.content || []);
        
        this.applyFilters();
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to load issues', error);
        this.isLoading.set(false);
      }
    });
  }


  // UI ACTIONS

  applyFilters(): void {
    let filtered = [...this.allIssues];

    // Filtro per Stato
    if (this.statusFilter() !== 'ALL') {
      filtered = filtered.filter(i => i.status === this.statusFilter());
    }

    // Filtro per Priorità
    if (this.priorityFilter() !== 'ALL') {
      filtered = filtered.filter(i => i.priority === this.priorityFilter());
    }

    this.issues.set(filtered);
  }

  onStatusChange(status: string): void {
    this.statusFilter.set(status);
    this.applyFilters();
  }

  onPriorityChange(priority: string): void {
    this.priorityFilter.set(priority);
    this.applyFilters();
  }

  sortData(field: string): void {
    if (field !== 'id') return;

    if (this.sortField() === 'id') {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set('id');
      this.sortDirection.set('asc');
    }

    const sorted = [...this.issues()].sort((a, b) => {
      const valA = a.id;
      const valB = b.id;
      const modifier = this.sortDirection() === 'asc' ? 1 : -1;
      return valA > valB ? (1 * modifier) : (-1 * modifier);
    });

    this.issues.set(sorted);
  }


  getPriorityStyle(priority: string): string {
    if (!priority) return 'bg-light text-secondary border';

    const p = priority.toLowerCase();
    switch (p) {
      case 'highest':
        return 'bg-danger text-white border-danger';
      case 'high':
        return 'bg-danger-subtle text-danger border-danger';
      case 'medium':
        return 'bg-warning-subtle text-warning border-warning';
      case 'low':
        return 'bg-success-subtle text-success border-success';
      case 'lowest':
        return 'bg-info-subtle text-info border-info';
      default:
        return 'bg-light text-secondary border';
    }
  }

  getStatusLabel(status: string): string {
    return status ? status.replace(/_/g, ' ').toUpperCase() : '';
  }

  getTagStyle(tagName: string): string {
    const styles: { [key: string]: string } = {
      'Security': 'bg-danger-subtle text-danger border-danger',
      'Mobile': 'bg-info-subtle text-info border-info',
      'UI/UX': 'bg-warning-subtle text-warning border-warning',
      'Content': 'bg-secondary-subtle text-secondary border-secondary',
      'Feature': 'bg-success-subtle text-success border-success'
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