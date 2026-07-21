# Frontend Pagination Implementation Guide

This document outlines the standard pagination pattern for the application. The system relies on Angular Signals, Standalone Components, and Bootstrap 5, mapping directly to Spring Data's `Page<T>` structure. 

This guide serves as the reference for implementing pagination across all list views (Projects, Issues, Users, Tags, etc.).

## 1. The Generic Page Model

All paginated HTTP responses must be typed using the generic `Page<T>` interface. This ensures consistency with the backend's Spring Boot `Pageable` responses.

**Location:** `src/app/core/models/page.model.ts`

```typescript
export interface Page<T> {
  content: T[];
  pageable: { pageNumber: number; pageSize: number };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  size: number;
  number: number;
}

```

## 2. The Pagination Component

The `PaginationComponent` is a presentation-only (dumb) component. It handles UI rendering, ellipsis truncation (showing a max of 5 page buttons), and boundary checks.

**Critical detail:** Spring Boot pagination is `0-indexed`, but the UI must display `1-indexed` page numbers to the user. The component handles this visual translation internally; **always pass and expect `0-indexed` values in your feature components.**

### API

**Inputs (Signals):**

* `currentPage`: `number` (0-indexed)
* `totalPages`: `number`
* `totalElements`: `number`

**Outputs:**

* `pageChange`: emits the new `number` (0-indexed) when a user navigates.

---

## 3. Implementation Pattern

Use the following pattern to integrate pagination into any feature component. This example uses `ProjectComponent`.

### Component State (TypeScript)

Use Signals to manage the pagination state and compute derived values to avoid null-checks in the template.

```typescript
import { Component, computed, signal, inject } from '@angular/core';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';

@Component({
  standalone: true,
  imports: [PaginationComponent, /* other imports */],
  templateUrl: './project.component.html'
})
export class ProjectComponent {
  private projectService = inject(ProjectService);

  // Pagination State
  public readonly currentPage = signal<number>(0);
  public readonly pageSize = signal<number>(12);

  // Data State
  public readonly projectsPage = signal<Page<ProjectResponse> | null>(null);

  // Computed Selectors
  public readonly projectList = computed(() => this.projectsPage()?.content ?? []);
  public readonly totalPages = computed(() => this.projectsPage()?.totalPages ?? 0);
  public readonly totalElements = computed(() => this.projectsPage()?.totalElements ?? 0);

  ngOnInit() {
    this.loadData();
  }

  private loadData(): void {
    this.projectService.getProjects(this.currentPage(), this.pageSize())
      .subscribe({
        next: (page) => this.projectsPage.set(page),
        error: (err) => console.error('Failed to fetch projects', err)
      });
  }

  public onPageChange(newPage: number): void {
    this.currentPage.set(newPage);
    this.loadData();
  }
}

```

### Template (HTML)

Place the `<app-pagination>` component below your list or grid. Use control flow to ensure it only renders when data is available.

```html
<!-- Data Grid -->
<div class="row g-4 mb-4">
  @for (project of projectList(); track project.id) {
    <!-- Card implementation -->
  }
</div>

<!-- Pagination Footer -->
@if (projectsPage() && projectList().length > 0) {
  <app-pagination
    [currentPage]="currentPage()"
    [totalPages]="totalPages()"
    [totalElements]="totalElements()"
    (pageChange)="onPageChange($event)"
  />
} @else if (projectList().length === 0) {
  <div class="text-center text-muted py-5">
    No items found.
  </div>
}

```

---

## Standard Practices

1. **State Management:** Keep `currentPage` and `pageSize` as dedicated `signal<number>` properties. Do not mutate the `projectsPage` object directly.
2. **Computed Safely:** Always use `computed()` with fallback values (`?? 0` or `?? []`) when extracting data from the `Page<T>` signal. This prevents template errors during the initial load when the page signal is `null`.
3. **Changing Page Size:** If implementing a page size selector (e.g., 10, 20, 50 items per page), ensure you reset the `currentPage` to `0` before triggering the new data fetch, otherwise you risk requesting an out-of-bounds page.

```typescript
public onPageSizeChange(newSize: number): void {
  this.pageSize.set(newSize);
  this.currentPage.set(0); // Reset boundary
  this.loadData();
}

```
