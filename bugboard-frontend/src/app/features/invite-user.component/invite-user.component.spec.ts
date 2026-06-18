import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InviteUserComponent } from './invite-user.component';

describe('InviteUserComponent', () => {
  let component: InviteUserComponent;
  let fixture: ComponentFixture<InviteUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InviteUserComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InviteUserComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
