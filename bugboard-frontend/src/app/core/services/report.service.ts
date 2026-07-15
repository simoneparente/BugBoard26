import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MonthlyProjectReportResponse } from '../report.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private http = inject(HttpClient);
  private readonly REPORT_API_URL = environment.reportApiUrl;

  /**
   * Generates and retrieves the monthly report for a specific project.
   */
  public getMonthlyReport(projectId: string): Observable<MonthlyProjectReportResponse> {
    return this.http.get<MonthlyProjectReportResponse>(`${this.REPORT_API_URL}/${projectId}`);
  }
}
