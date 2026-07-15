import { Component, inject } from '@angular/core';
import { BrandLogoComponent } from '../../layout/brand-logo.component/brand-logo.component';
import { InviteUserComponent } from '../invite-user.component/invite-user.component';
import { AuthService } from '../../core/auth/auth-service';
import { ProjectComponent } from '../project.component/project.component';

@Component({
  selector: 'app-dashboard.component',
  imports: [BrandLogoComponent, InviteUserComponent, ProjectComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private authService = inject(AuthService);

  public logout() {
    this.authService.logout();
  }
}
