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
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(IssueComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update searchQuery and reset page on onSearchInput', () => {
    const spy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});
    component.onSearchInput('login');

    expect(component.searchQuery()).toBe('login');
    expect(component.currentPage()).toBe(0);
    expect(spy).toHaveBeenCalled();
  });

  it('should clear searchQuery on clearSearch', () => {
    component.searchQuery.set('login');
    const spy = vi.spyOn(component, 'loadIssues').mockImplementation(() => {});

    component.clearSearch();

    expect(component.searchQuery()).toBe('');
    expect(component.currentPage()).toBe(0);
    expect(spy).toHaveBeenCalled();
  });
});
