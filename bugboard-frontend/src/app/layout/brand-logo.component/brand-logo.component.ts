import { Component, input, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-brand-logo',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './brand-logo.component.html',
  styleUrl: './brand-logo.component.scss',
})
export class BrandLogoComponent {
  private readonly router = inject(Router);
  theme = input<'light' | 'dark'>('light');

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }
}
