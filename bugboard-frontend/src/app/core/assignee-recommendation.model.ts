import { UserResponse } from './auth/auth.models';

export interface AssigneeRecommendation {
  user: UserResponse;
  workloadScore: number;
  activeIssueCount: number;
}
