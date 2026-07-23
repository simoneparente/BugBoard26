import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of } from 'rxjs';

import { IssueComponent } from './issue.component';
import { IssueService } from '../../core/services/issue.service';

describe('IssueComponent', () => {
  let component: IssueComponent;
  let fixture: ComponentFixture<IssueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IssueComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(IssueComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update searchQuery and reset page on onSearchInput', () => {
    vi.useFakeTimers();
    const issueService = TestBed.inject(IssueService);
    const spy = vi
      .spyOn(issueService, 'getIssuesByProject')
      .mockReturnValue(of({ content: [], totalPages: 0, totalElements: 0 } as any));

    component.projectId = 'proj-1';
    component.onSearchInput('login');

    expect(component.searchQuery()).toBe('login');
    expect(component.currentPage()).toBe(0);

    vi.advanceTimersByTime(300);
    expect(spy).toHaveBeenCalledWith('proj-1', 'ALL', 'ALL', 'login', 0, 10, 'title', 'asc');
    vi.useRealTimers();
  });

  it('should clear searchQuery on clearSearch', () => {
    component.searchQuery.set('login');
    const spy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});

    component.clearSearch();

    expect(component.searchQuery()).toBe('');
    expect(component.currentPage()).toBe(0);
    expect(spy).toHaveBeenCalled();
  });

  it('should trigger search immediately on onSearchEnter', () => {
    component.searchQuery.set('error');
    const spy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});

    component.onSearchEnter();

    expect(component.currentPage()).toBe(0);
    expect(spy).toHaveBeenCalled();
  });
});

// Additional tests to increase coverage

describe('IssueComponent additional behavior', () => {
  let component: IssueComponent;
  let fixture: ComponentFixture<IssueComponent>;
  let issueService: IssueService;
  let projectService: ProjectService;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IssueComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { isReadonly: vi.fn(() => false) },
        },
        {
          provide: ProjectService,
          useValue: { getById: vi.fn(() => of({ name: 'Test Project' })) },
        },
        {
          provide: IssueService,
          useValue: {
            getIssuesByProject: vi.fn(() =>
              of({ content: [], totalPages: 0, totalElements: 0 } as any),
            ),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(IssueComponent);
    component = fixture.componentInstance;
    issueService = TestBed.inject(IssueService);
    projectService = TestBed.inject(ProjectService);
    authService = TestBed.inject(AuthService);
    // Set a projectId for the component
    component.projectId = 'proj-123';
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should load project name on init', () => {
    const spy = vi.spyOn(projectService, 'getById').mockReturnValue(of({ name: 'Demo Project' }));
    component.ngOnInit();
    expect(spy).toHaveBeenCalledWith('proj-123');
    expect(component.projectName()).toBe('Demo Project');
  });

  it('sortData should toggle direction on same field', () => {
    const loadSpy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});
    component.sortField.set('title');
    component.sortDirection.set('asc');
    component.sortData('title');
    expect(component.sortDirection()).toBe('desc');
    expect(loadSpy).toHaveBeenCalled();
  });

  it('sortData should change field and reset direction', () => {
    const loadSpy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});
    component.sortField.set('title');
    component.sortDirection.set('asc');
    component.sortData('assignee');
    expect(component.sortField()).toBe('assignee.username');
    expect(component.sortDirection()).toBe('asc');
    expect(loadSpy).toHaveBeenCalled();
  });

  it('getPriorityStyle returns correct class', () => {
    expect(component.getPriorityStyle('')).toBe('priority-lowest');
    expect(component.getPriorityStyle('HIGH')).toBe('priority-high');
  });

  it('getStatusBadgeClass maps statuses correctly', () => {
    expect(component.getStatusBadgeClass('NEW')).toBe('status-badge status-to-do');
    expect(component.getStatusBadgeClass('IN_PROGRESS')).toBe('status-badge status-in-progress');
    expect(component.getStatusBadgeClass('COMPLETED')).toBe('status-badge status-completed');
    expect(component.getStatusBadgeClass('UNKNOWN')).toBe('status-badge status-to-do');
  });

  it('getTagStyle returns style for known and unknown tags', () => {
    expect(component.getTagStyle('Security')).toBe('bg-danger-subtle text-danger border-danger');
    expect(component.getTagStyle('NonExistent')).toBe('bg-light text-secondary border');
  });

  it('onActionClick stops propagation and logs', () => {
    const event = { stopPropagation: vi.fn() } as unknown as Event;
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => {});
    component.onActionClick(event, 'issue-1');
    expect(event.stopPropagation).toHaveBeenCalled();
    expect(logSpy).toHaveBeenCalledWith('Action clicked for:', 'issue-1');
    logSpy.mockRestore();
  });

  it('editIssue and deleteIssue log correctly', () => {
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => {});
    component.editIssue('e1');
    component.deleteIssue('d1');
    expect(logSpy).toHaveBeenCalledWith('Edit issue:', 'e1');
    expect(logSpy).toHaveBeenCalledWith('Delete issue:', 'd1');
    logSpy.mockRestore();
  });
});
