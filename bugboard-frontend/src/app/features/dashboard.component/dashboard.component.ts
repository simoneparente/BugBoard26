import { Component } from '@angular/core';
import { BrandLogoComponent } from "../../layout/brand-logo.component/brand-logo.component";

@Component({
  selector: 'app-dashboard.component',
  imports: [BrandLogoComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {}
