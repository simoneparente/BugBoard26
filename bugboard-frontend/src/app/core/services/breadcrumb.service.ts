import { Injectable, inject, signal } from '@angular/core';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';
import { filter } from 'rxjs';

export interface BreadcrumbItem {
  label: string;
  url?: string;
  icon?: string;
}

@Injectable({
  providedIn: 'root',
})
export class BreadcrumbService {
  private readonly router = inject(Router);

  public readonly breadcrumbs = signal<BreadcrumbItem[]>([]);
  public readonly projectName = signal<string | null>(null);

  constructor() {
    this.updateBreadcrumbs(this.router.url);

    this.router.events
      .pipe(filter((event): event is NavigationStart => event instanceof NavigationStart))
      .subscribe(() => {
        this.projectName.set(null);
      });

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updateBreadcrumbs(event.urlAfterRedirects || event.url);
      });
  }

  public setProjectName(name: string | null) {
    this.projectName.set(name);
    this.updateBreadcrumbs(this.router.url);
  }

  private updateBreadcrumbs(url: string) {
    const cleanUrl = url.split('?')[0].split('#')[0];
    const segments = cleanUrl.split('/').filter(Boolean);

    const items: BreadcrumbItem[] = [];

    // Root level: Dashboard
    items.push({
      label: 'Dashboard',
      url: '/dashboard',
      icon: 'bi-house-door',
    });

    if (segments.length === 0 || segments[0] === 'dashboard') {
      this.breadcrumbs.set(items);
      return;
    }

    if (segments[0] === 'projects') {
      if (segments.length === 1) {
        // Just /projects, we can redirect or ignore it, as dashboard is projects.
      } else if (segments.length === 2 && segments[1] === 'create') {
        items.push({
          label: 'Create Project',
          icon: 'bi-plus-circle',
        });
      } else if (segments.length >= 2 && segments[1] !== 'create') {
        const projectId = segments[1];
        const currentProjectLabel = this.projectName() ?? 'Issue Tracker';

        items.push({
          label: currentProjectLabel,
          url: `/projects/${projectId}/issues`,
          icon: 'bi-kanban',
        });

        if (segments.length >= 3) {
          const projectFeature = segments[2];

          if (projectFeature === 'issues') {
            if (segments.length === 4) {
              if (segments[3] === 'create') {
                items.push({
                  label: 'Create Issue',
                  icon: 'bi-bug',
                });
              } else {
                items.push({
                  label: `Issue #${segments[3].substring(0, 4)}`,
                  url: `/projects/${projectId}/issues/${segments[3]}`,
                  icon: 'bi-ticket-detailed',
                });
              }
            }
          } else if (projectFeature === 'report') {
            items.push({
              label: 'Report',
              icon: 'bi-file-earmark-bar-graph',
            });
          } else if (projectFeature === 'tags') {
            items.push({
              label: 'Tags',
              icon: 'bi-tags',
            });
          }
        }
      }
    } else {
      // Fallback for other routes
      let currentPath = '';
      for (const seg of segments) {
        currentPath += `/${seg}`;
        const formattedLabel = seg
          .replace(/-/g, ' ')
          .replace(/\b\w/g, (char) => char.toUpperCase());

        items.push({
          label: formattedLabel,
          url: currentPath,
        });
      }
    }

    this.breadcrumbs.set(items);
  }
}
