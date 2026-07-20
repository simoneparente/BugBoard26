import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { InviteUserComponent } from '../../features/invite-user.component/invite-user.component';
import { BrandLogoComponent } from '../brand-logo.component/brand-logo.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, InviteUserComponent, BrandLogoComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  private readonly authService = inject(AuthService);

  public logout() {
    this.authService.logout();
  }
}
