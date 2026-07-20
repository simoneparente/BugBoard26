import { AttachmentResponse } from "./attachments.model";
import { TagResponse } from "./tag.model";

export interface IssueResponse {
  id: string;
  title: string;
  description: string;
  createdAt: string;
  updatedAt: string; 
  status: string;
  priority: string;
  type: string;
  assigneeUsername: string | null;
  tags: TagResponse[];
  attachments: AttachmentResponse[];
  projectId: string;
  projectName: string;
}