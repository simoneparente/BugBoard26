import { AttachmentResponse } from './attachments.model';
import { TagResponse } from './tag.model';

export interface IssueRequest {
  title: string;
  description: string;
  assigneeUsername?: string;
  status?: string;
  type: string;
  priority: string;
  tags?: any[];
  attachments?: any[];
}

export interface IssueResponse {
  id: string;
  sequenceNumber: number;
  title: string;
  description: string;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
  status: string;
  priority: string;
  type: string;
  assigneeUsername: string | null;
  tags: TagResponse[];
  attachments: AttachmentResponse[];
  projectId: string;
  projectKey: string;
  projectName: string;
}
