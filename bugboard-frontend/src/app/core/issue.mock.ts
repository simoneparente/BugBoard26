import { IssueResponse } from './issue.model';

export const MOCK_ISSUES: IssueResponse[] = [
  {
    id: '1',
    title: 'Fix navigation bar bug',
    description: 'The navbar is not responsive on mobile.',
    createdAt: '2026-07-15T10:00:00Z',
    updatedAt: '2026-07-15T10:00:00Z',
    status: 'OPEN',
    priority: 'HIGH',
    type: 'BUG',
    assigneeUsername: 'michela',
    tags: [{ id: 't1', name: 'UI', color: 'blue', projectId: 'p1' }],
    attachments: [],
    projectId: 'p1',
    projectName: 'STREETCATS'
  },
  {
    id: '2',
    title: 'Add cat tracking map',
    description: 'Integrate leaflet map for sightings.',
    createdAt: '2026-07-14T15:30:00Z',
    updatedAt: '2026-07-14T15:30:00Z',
    status: 'IN_PROGRESS',
    priority: 'MEDIUM',
    type: 'FEATURE',
    assigneeUsername: null,
    tags: [{ id: 't2', name: 'Feature', color: 'green', projectId: 'p1' }],
    attachments: [],
    projectId: 'p1',
    projectName: 'STREETCATS'
  }
];