import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectResponse } from '../../core/project.model';
import { AuthService } from '../../core/auth/auth-service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
})
export class ProjectsComponent implements OnInit {
  private readonly authService = inject(AuthService);

  // In un'app reale, questo deriverà dal tuo ProjectService tramite chiamata HTTP
  public readonly projects = signal<ProjectResponse[]>([]);
  
  // Computed property: true se l'utente loggato è ADMIN
  public readonly isAdmin = computed(() => this.authService.currentUser()?.role === 'ADMIN');

  // Statistiche globali calcolate dinamicamente in base ai progetti caricati
  public readonly globalStats = computed(() => {
    const allIssues = this.projects().flatMap(p => p.issues);
    const resolved = allIssues.filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length;
    const total = allIssues.length;
    const health = total === 0 ? 100 : Math.round((resolved / total) * 100);
    
    return { resolved, pending: total - resolved, health };
  });

  ngOnInit(): void {
    this.loadMockData();
  }

  /**
   * Calcola la percentuale di completamento di un singolo progetto
   */
  public getProjectProgress(project: ProjectResponse): number {
    if (!project.issues || project.issues.length === 0) return 0;
    const resolved = project.issues.filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length;
    return Math.round((resolved / project.issues.length) * 100);
  }

  /**
   * Restituisce i bug risolti vs totali in formato testuale
   */
  public getResolutionStats(project: ProjectResponse): string {
    const resolved = project.issues.filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length;
    return `${resolved}/${project.issues.length}`;
  }

  /**
   * Determina lo stile visivo della card (colore/icona) in base allo stato delle issue
   */
  public getProjectTheme(project: ProjectResponse) {
    const hasCritical = project.issues.some(i => i.priority === 'CRITICAL' && i.status !== 'CLOSED');
    const progress = this.getProjectProgress(project);

    if (hasCritical) return { class: 'error', icon: 'terminal', label: 'Critical' };
    if (progress === 100 && project.issues.length > 0) return { class: 'tertiary', icon: 'cloud_done', label: 'Maintenance' };
    if (progress === 0) return { class: 'primary-fixed', icon: 'security', label: 'Planning' };
    return { class: 'secondary', icon: 'layers', label: 'In Progress' };
  }

  private loadMockData(): void {
    // Dati fittizi per visualizzare la griglia in attesa del backend
    this.projects.set([
      {
        id: '1',
        name: 'BugBoard26 Core Engine',
        description: 'Backend stability and infrastructure scaling for the v4.0 release branch.',
        createdAt: new Date().toISOString(),
        issues: [
          { id: '1', title: 'DB Timeout', description: '', createdAt: '', updatedAt: '', status: 'OPEN', priority: 'CRITICAL', type: 'BUG', assigneeUsername: 'Mario Penna', tags: [], attachments: [], projectId: '1', projectName: 'BugBoard26 Core Engine' },
          { id: '2', title: 'Refactor Auth', description: '', createdAt: '', updatedAt: '', status: 'RESOLVED', priority: 'HIGH', type: 'TASK', assigneeUsername: 'Michela Pollio', tags: [], attachments: [], projectId: '1', projectName: 'BugBoard26 Core Engine' }
        ]
      },
      {
        id: '2',
        name: 'UI/UX Refresh 2026',
        description: 'Transitioning legacy dashboard components to the Corporate Modernism design system.',
        createdAt: new Date().toISOString(),
        issues: [
          { id: '3', title: 'Update Tailwind', description: '', createdAt: '', updatedAt: '', status: 'RESOLVED', priority: 'MEDIUM', type: 'TASK', assigneeUsername: 'Mario Penna', tags: [], attachments: [], projectId: '2', projectName: 'UI/UX Refresh 2026' }
        ]
      }
    ]);
  }
}