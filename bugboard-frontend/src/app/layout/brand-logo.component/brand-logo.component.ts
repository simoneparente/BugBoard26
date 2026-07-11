import { Component, input } from '@angular/core';

@Component({
  selector: 'app-brand-logo',
  standalone: true,
  imports: [],
  templateUrl: './brand-logo.component.html',
  styleUrl: './brand-logo.component.scss',
})
export class BrandLogoComponent {
  theme = input<'light' | 'dark'>('light');
}
