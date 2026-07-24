import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of } from 'rxjs';
import { signal } from '@angular/core';

import { IssueComponent } from './issue.component';
import { IssueService } from '../../core/services/issue.service';
import { ProjectService } from '../../core/services/project.service';
import { AuthService } from '../../core/auth/auth-service';

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

    component.projectKey = 'PRJ';
    component.onSearchInput('login');

    expect(component.searchQuery()).toBe('login');
    expect(component.currentPage()).toBe(0);

    vi.advanceTimersByTime(300);
    expect(spy).toHaveBeenCalledWith('PRJ', 'ALL', 'ALL', 'ALL', 'login', 0, 10, 'title', 'asc');
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
          useValue: { isReadonly: signal(false), isAdmin: signal(false) },
        },
        {
          provide: ProjectService,
          useValue: { getById: vi.fn(() => of({ id: 'proj-123', name: 'Test Project' } as any)) },
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
    component.projectKey = 'PRJ';
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should load project name on init', () => {
    const spy = vi
      .spyOn(projectService, 'getById')
      .mockReturnValue(of({ id: 'proj-123', name: 'Demo Project' } as any));
    component.ngOnInit();
    expect(spy).toHaveBeenCalledWith('PRJ');
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

  it('onTypeChange should update filter, reset page, and reload issues', () => {
    const loadSpy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});
    component.currentPage.set(3);

    component.onTypeChange('BUG');

    expect(component.typeFilter()).toBe('BUG');
    expect(component.currentPage()).toBe(0);
    expect(loadSpy).toHaveBeenCalled();
  });

  it('getPriorityStyle returns correct class', () => {
    expect(component.getPriorityStyle('')).toBe('priority-lowest');
    expect(component.getPriorityStyle('HIGH')).toBe('priority-high');
  });

  it('getPriorityIcon returns correct icon', () => {
    expect(component.getPriorityIcon('HIGHEST')).toBe('bi-chevron-double-up');
    expect(component.getPriorityIcon('HIGH')).toBe('bi-chevron-up');
    expect(component.getPriorityIcon('MEDIUM')).toBe('bi-dash-lg');
    expect(component.getPriorityIcon('ALL')).toBe('bi-sliders');
  });

  it('getStatusIcon returns correct icon', () => {
    expect(component.getStatusIcon('TO_DO')).toBe('bi-circle');
    expect(component.getStatusIcon('IN_PROGRESS')).toBe('bi-hourglass-split');
    expect(component.getStatusIcon('MARKED_FOR_REVIEW')).toBe('bi-eye-fill');
    expect(component.getStatusIcon('COMPLETED')).toBe('bi-check-circle-fill');
    expect(component.getStatusIcon('ALL')).toBe('bi-funnel');
  });

  it('getTypeIcon returns correct icon', () => {
    expect(component.getTypeIcon('BUG')).toBe('bi-bug-fill text-danger');
    expect(component.getTypeIcon('FEATURE')).toBe('bi-star-fill text-primary');
    expect(component.getTypeIcon('QUESTION')).toBe('bi-question-circle-fill text-warning');
    expect(component.getTypeIcon('DOCUMENTATION')).toBe('bi-file-earmark-text-fill text-info');
    expect(component.getTypeIcon('ALL')).toBe('bi-tag-fill text-secondary');
  });

  it('getTypeBadgeClass maps types like issue detail', () => {
    expect(component.getTypeBadgeClass('BUG')).toBe(
      'bg-danger-subtle text-danger border border-danger-subtle',
    );
    expect(component.getTypeBadgeClass('FEATURE')).toBe(
      'bg-primary-subtle text-primary border border-primary-subtle',
    );
    expect(component.getTypeBadgeClass('UNKNOWN')).toBe(
      'bg-secondary-subtle text-secondary border border-secondary-subtle',
    );
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
});
