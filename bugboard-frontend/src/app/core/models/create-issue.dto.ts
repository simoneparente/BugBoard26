export interface CreateIssueDto {
  title: string;
  description: string;
  assigneeUsername?: string;
  status?: string;
  type: string;
  priority: string;
  tags?: any[];
  attachments?: any[];
}
