import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MonthlyProjectReportResponse } from '../report.models';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private readonly api = inject(ApiService);

  /**
   * Generates and retrieves the monthly report for a specific project.
   */
  public getMonthlyReport(projectId: string): Observable<MonthlyProjectReportResponse> {
    return this.api.reports.getMonthlyReport(projectId);
  }
}
