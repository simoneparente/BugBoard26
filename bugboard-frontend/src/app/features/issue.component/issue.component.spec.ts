import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IssueComponent } from './issue.component';

describe('IssueComponent', () => {
  let component: IssueComponent;
  let fixture: ComponentFixture<IssueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IssueComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(IssueComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
