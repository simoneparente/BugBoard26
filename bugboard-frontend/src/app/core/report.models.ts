export interface UserMonthlyProjectReportResponse {
  userId: string;
  userName: string;
  referenceMonth: number;
  referenceYear: number;
  openedBugs: number;
  managedBugs: number;
  averageResolutionTime: number;
}

export interface MonthlyProjectReportResponse {
  projectId: string;
  projectName: string;
  referenceMonth: number;
  referenceYear: number;
  openedBugs: number;
  managedBugs: number;
  averageResolutionTime: number;
  userMonthlyReports: UserMonthlyProjectReportResponse[];
}
