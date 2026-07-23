import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { IssueService } from './issue.service';

describe('IssueService', () => {
  let service: IssueService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(IssueService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should include search parameter when fetching issues by project', () => {
    const projectId = 'proj-123';
    service.getIssuesByProject(projectId, 'ALL', 'ALL', 'keyword').subscribe();

    const req = httpTestingController.expectOne(
      (request) =>
        request.url.includes(`/api/projects/${projectId}/issues`) &&
        request.params.get('search') === 'keyword',
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0 });
  });
});
