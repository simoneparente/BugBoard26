import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReportService } from '../../core/services/report.service';
import {
  MonthlyProjectReportResponse,
  UserMonthlyProjectReportResponse,
} from '../../core/report.models';

import { BreadcrumbService } from '../../core/services/breadcrumb.service';

@Component({
  selector: 'app-report',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './report.component.html',
  styleUrl: './report.component.scss',
})
export class ReportComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly reportService = inject(ReportService);
  private readonly breadcrumbService = inject(BreadcrumbService);

  public readonly isLoading = signal<boolean>(true);
  public readonly errorMessage = signal<string | null>(null);
  public readonly report = signal<MonthlyProjectReportResponse | null>(null);

  /**
   * Returns the English month name from a month number (1-12).
   */
  public readonly monthName = computed(() => {
    const r = this.report();
    if (!r) return '';
    const monthNames = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];
    return monthNames[r.referenceMonth - 1] ?? '';
  });

  /**
   * Formats the average resolution time as a human-readable string.
   */
  public formatAvgTime(hours: number): string {
    if (hours === 0) return '0h';
    if (hours < 1) return `${Math.round(hours * 60)}m`;
    return `${hours.toFixed(1)}h`;
  }

  /**
   * Generates a CSS width percentage for the progress bar.
   * Maps the value to a 0-100% range based on the max in the dataset.
   */
  public getProgressWidth(value: number, maxValue: number): string {
    if (maxValue === 0) return '0%';
    const pct = Math.min((value / maxValue) * 100, 100);
    return `${Math.max(pct, 5)}%`; // Minimum 5% so the bar is always visible
  }

  /**
   * Returns a user's initials (first 2 chars of username, uppercased).
   */
  public getInitials(userName: string): string {
    return userName.substring(0, 2).toUpperCase();
  }

  /**
   * Computes maximum values across user reports, used for progress bar scaling.
   */
  public readonly maxOpened = computed(() => {
    const r = this.report();
    if (!r || r.userMonthlyReports.length === 0) return 1;
    return Math.max(...r.userMonthlyReports.map((u) => u.openedBugs), 1);
  });

  public readonly maxManaged = computed(() => {
    const r = this.report();
    if (!r || r.userMonthlyReports.length === 0) return 1;
    return Math.max(...r.userMonthlyReports.map((u) => u.managedBugs), 1);
  });

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('projectId');
    if (!projectId) {
      this.isLoading.set(false);
      this.errorMessage.set('Missing Project ID in URL.');
      return;
    }

    this.reportService.getMonthlyReport(projectId).subscribe({
      next: (data) => {
        this.report.set(data);
        this.breadcrumbService.setProjectName(data.projectName);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.errorMessage.set('Project not found. Check the ID and try again.');
        } else {
          this.errorMessage.set('Error loading report. Please try again later.');
        }
      },
    });
  }
}
