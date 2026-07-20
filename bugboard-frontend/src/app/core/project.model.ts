import { IssueResponse } from './issue.model';

export interface ProjectResponse {
  id: string;
  name: string;
  description: string;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
  issues?: IssueResponse[];
}
