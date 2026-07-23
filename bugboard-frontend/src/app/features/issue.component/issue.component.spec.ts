import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IssueComponent } from './issue.component';

import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

describe('IssueComponent', () => {
  let component: IssueComponent;
  let fixture: ComponentFixture<IssueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IssueComponent],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(IssueComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
