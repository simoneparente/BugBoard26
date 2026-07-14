import { IssueResponse } from "./issue.model";

export interface ProjectResponse {
  id: string;
  name: string;
  description: string;
  createdAt: string; // ISO date string
  issues?: IssueResponse[];
}