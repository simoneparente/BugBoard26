import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { IssueComponent } from './issue.component';

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
    const spy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});
    component.onSearchInput('login');

    expect(component.searchQuery()).toBe('login');
    vi.advanceTimersByTime(300);
    expect(component.currentPage()).toBe(0);
    expect(spy).toHaveBeenCalled();
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
