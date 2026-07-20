import { Component } from '@angular/core';
import { ProjectComponent } from '../project.component/project.component';

@Component({
  selector: 'app-dashboard.component',
  imports: [ProjectComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {}
