import { Component } from '@angular/core';
import { BrandLogoComponent } from '../../layout/brand-logo.component/brand-logo.component';
import { InviteUserComponent } from "../invite-user.component/invite-user.component";

@Component({
  selector: 'app-dashboard.component',
  imports: [BrandLogoComponent, InviteUserComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {}
