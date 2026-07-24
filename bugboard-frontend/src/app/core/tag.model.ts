export interface TagResponse {
  id: string;
  name: string;
  color: string;
  projectKey: string;
}

export interface TagRequest {
  name: string;
  color: string;
  projectKey: string;
}
