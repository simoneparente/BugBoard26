export interface TagResponse {
  id: string;
  name: string;
  color: string;
  projectId: string;
}

export interface TagRequest {
  name: string;
  color: string;
  projectId: string;
}
