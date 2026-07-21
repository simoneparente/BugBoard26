import { Component, input, output, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Reusable standalone pagination component
 * Displays Bootstrap 5 pagination UI for paginated data
 *
 * Features:
 * - Input signals: currentPage, totalPages, totalElements
 * - Output signal: pageChange event
 * - Smart page number calculation (shows max 5 pages)
 * - Handles edge cases for first/last page
 * - Converts backend 0-indexed pages to 1-indexed UI display
 */
@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginationComponent {
  /**
   * Current 0-indexed page number from backend
   */
  currentPage = input.required<number>();

  /**
   * Total number of pages available
   */
  totalPages = input.required<number>();

  /**
   * Total number of elements across all pages
   * Used for display purposes (e.g., "Showing 1-12 of 45 items")
   */
  totalElements = input.required<number>();

  /**
   * Emits when user changes page (0-indexed to match backend)
   */
  pageChange = output<number>();

  /**
   * Calculate array of visible page numbers to display
   * Shows max 5 pages at a time with ellipsis when needed
   *
   * Examples:
   * - 5 or fewer total pages: [0, 1, 2, 3, 4]
   * - On page 0 of 10 pages: [0, 1, 2, 3, 4]
   * - On page 5 of 10 pages: [3, 4, 5, 6, 7]
   * - On page 9 of 10 pages: [5, 6, 7, 8, 9]
   */
  visiblePages = computed(() => {
    const current = this.currentPage();
    const total = this.totalPages();

    // If 5 or fewer pages, show all
    if (total <= 5) {
      return Array.from({ length: total }, (_, i) => i);
    }

    // Calculate range: center around current page, show 5 pages max
    const halfRange = 2; // Show 2 pages on each side of current
    let start = Math.max(0, current - halfRange);
    let end = Math.min(total - 1, current + halfRange);

    // Adjust if near the boundaries
    if (start === 0) {
      end = Math.min(total - 1, 4);
    }
    if (end === total - 1) {
      start = Math.max(0, total - 5);
    }

    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  });

  /**
   * Check if we should show "..." before visible pages
   * (Indicates there are hidden pages at the beginning)
   */
  showEllipsisBefore = computed(() => this.visiblePages()[0] > 0 && this.totalPages() > 5);

  /**
   * Check if we should show "..." after visible pages
   * (Indicates there are hidden pages at the end)
   */
  showEllipsisAfter = computed(() => {
    const lastVisiblePage = this.visiblePages()[this.visiblePages().length - 1];
    return lastVisiblePage < this.totalPages() - 1 && this.totalPages() > 5;
  });

  /**
   * Check if "Previous" button should be disabled
   */
  isPrevDisabled = computed(() => this.currentPage() === 0);

  /**
   * Check if "Next" button should be disabled
   */
  isNextDisabled = computed(() => this.currentPage() === this.totalPages() - 1);

  /**
   * Handle previous page click
   */
  onPrevious(): void {
    if (!this.isPrevDisabled()) {
      this.pageChange.emit(this.currentPage() - 1);
    }
  }

  /**
   * Handle next page click
   */
  onNext(): void {
    if (!this.isNextDisabled()) {
      this.pageChange.emit(this.currentPage() + 1);
    }
  }

  /**
   * Handle specific page number click
   * @param pageNumber 0-indexed page number from backend
   */
  onPageClick(pageNumber: number): void {
    if (pageNumber !== this.currentPage()) {
      this.pageChange.emit(pageNumber);
    }
  }

  /**
   * Convert 0-indexed backend page to 1-indexed display page
   * @param zeroIndexedPage 0-indexed page number
   * @returns 1-indexed page number for display
   */
  displayPageNumber(zeroIndexedPage: number): number {
    return zeroIndexedPage + 1;
  }
}
