import { Injectable, inject, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
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

  constructor() {
    this.updateBreadcrumbs(this.router.url);

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updateBreadcrumbs(event.urlAfterRedirects || event.url);
      });
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

    // If on Dashboard or /projects root list, stop at Dashboard
    if (
      segments.length === 0 ||
      segments[0] === 'dashboard' ||
      (segments.length === 1 && segments[0] === 'projects')
    ) {
      this.breadcrumbs.set(items);
      return;
    }

    if (segments[0] === 'projects') {
      if (segments.length === 2 && segments[1] === 'create') {
        items.push({
          label: 'Create Project',
          icon: 'bi-plus-circle',
        });
      } else if (segments.length >= 2 && segments[1] !== 'create') {
        const projectId = segments[1];
        items.push({
          label: 'Project Details',
          url: `/projects/${projectId}`,
          icon: 'bi-kanban',
        });

        if (segments.length === 4 && segments[2] === 'issues' && segments[3] === 'create') {
          items.push({
            label: 'Create Issue',
            icon: 'bi-bug',
          });
        }
      }
    } else if (segments[0] === 'reports') {
      if (segments.length >= 2) {
        const projectId = segments[1];
        items.push({
          label: 'Project Details',
          url: `/projects/${projectId}`,
          icon: 'bi-kanban',
        });
        items.push({
          label: 'Monthly Report',
          icon: 'bi-file-earmark-bar-graph',
        });
      }
    } else {
      // Fallback for custom routes
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
