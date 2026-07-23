import { UserResponse } from './auth/auth.models';
import { IssueResponse } from './issue.model';
import { TagResponse } from './tag.model';

export interface ProjectResponse {
  id: string;
  name: string;
  description: string;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
  issues?: IssueResponse[];
  members?: UserResponse[];
  tags?: TagResponse[];
}
