import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectResponse } from '../../core/project.model';
import { AuthService } from '../../core/auth/auth-service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-project',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
})
export class ProjectComponent implements OnInit {
  private readonly authService = inject(AuthService);

  public readonly projects = signal<ProjectResponse[]>([]);
  
  // Computed property per verificare se l'utente è ADMIN
  public readonly isAdmin = computed(() => {
    const user = this.authService.currentUser();
    return user?.role === 'ADMIN';
  });

  // Calcolo dinamico dello stato di salute globale
  public readonly globalStats = computed(() => {
    const allIssues = this.projects().flatMap(p => p.issues || []);
    const total = allIssues.length;
    const resolved = allIssues.filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length;
    const health = total === 0 ? 100 : Math.round((resolved / total) * 100);
    
    return { resolved, pending: total - resolved, health };
  });

  ngOnInit(): void {
    // Sostituirai questo con this.projectService.getProjects().subscribe(...)
    this.loadMockData();
  }

  public getProjectProgress(project: ProjectResponse): number {
    if (!project.issues || project.issues.length === 0) return 0;
    const resolved = project.issues.filter((i) => i.status === 'RESOLVED' || i.status === 'CLOSED').length;
    return Math.round((resolved / project.issues.length) * 100);
  }

  public getResolutionStats(project: ProjectResponse): string {
    const resolved = project.issues?.filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length || 0;
    const total = project.issues?.length || 0;
    return `${resolved}/${total}`;
  }

  // Mappa gli stati su colori Bootstrap nativi (danger, primary, success, warning)
  public getProjectTheme(project: ProjectResponse) {
    const issues = project.issues || [];
    const hasCritical = issues.some((i) => i.priority === 'CRITICAL' && i.status !== 'CLOSED');
    const progress = this.getProjectProgress(project);

    if (hasCritical) return { color: 'danger', icon: 'terminal', label: 'Critical' };
    if (progress === 100 && issues.length > 0) return { color: 'success', icon: 'cloud_done', label: 'Maintenance' };
    if (progress === 0) return { color: 'info', icon: 'security', label: 'Planning' };
    return { color: 'primary', icon: 'layers', label: 'In Progress' };
  }

  private loadMockData(): void {
    this.projects.set([
      {
        id: '1',
        name: 'Nexus Core Engine',
        description: 'Backend stability and infrastructure scaling for the v4.0 release branch.',
        createdAt: new Date().toISOString(),
        issues: [
          { id: '1', title: 'Memory leak on startup', description: '', createdAt: '', updatedAt: '', status: 'OPEN', priority: 'CRITICAL', type: 'BUG', assigneeUsername: null, tags: [], attachments: [], projectId: '1', projectName: 'Nexus Core Engine' },
          { id: '2', title: 'Refactor auth module', description: '', createdAt: '', updatedAt: '', status: 'RESOLVED', priority: 'HIGH', type: 'TASK', assigneeUsername: null, tags: [], attachments: [], projectId: '1', projectName: 'Nexus Core Engine' }
        ]
      },
      {
        id: '2',
        name: 'UI/UX Refresh 2026',
        description: 'Transitioning legacy dashboard components to the Corporate Modernism design system.',
        createdAt: new Date().toISOString(),
        issues: [
          { id: '3', title: 'Update color palette', description: '', createdAt: '', updatedAt: '', status: 'RESOLVED', priority: 'MEDIUM', type: 'TASK', assigneeUsername: null, tags: [], attachments: [], projectId: '2', projectName: 'UI/UX Refresh 2026' }
        ]
      },
      {
        id: '2',
        name: 'UI/UX Refresh 2026',
        description: 'Transitioning legacy dashboard components to the Corporate Modernism design system.',
        createdAt: new Date().toISOString(),
        issues: [
          { id: '3', title: 'Update color palette', description: '', createdAt: '', updatedAt: '', status: 'RESOLVED', priority: 'MEDIUM', type: 'TASK', assigneeUsername: null, tags: [], attachments: [], projectId: '2', projectName: 'UI/UX Refresh 2026' }
        ]
      },
      {
        id: '2',
        name: 'UI/UX Refresh 2026',
        description: 'Transitioning legacy dashboard components to the Corporate Modernism design system.',
        createdAt: new Date().toISOString(),
        issues: [
          { id: '3', title: 'Update color palette', description: '', createdAt: '', updatedAt: '', status: 'RESOLVED', priority: 'MEDIUM', type: 'TASK', assigneeUsername: null, tags: [], attachments: [], projectId: '2', projectName: 'UI/UX Refresh 2026' }
        ]
      }
    ]);
  }
}