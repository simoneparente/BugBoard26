import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { IssueCreateComponent } from './issue-create.component';

describe('IssueCreateComponent', () => {
  let component: IssueCreateComponent;
  let fixture: ComponentFixture<IssueCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IssueCreateComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(IssueCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
