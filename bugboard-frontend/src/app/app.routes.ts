import { Routes, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './core/auth/auth-service';
import { LoginComponent } from './features/login.component/login.component';

import { DashboardComponent } from './features/dashboard.component/dashboard.component';
import { RegisterComponent } from './features/register.component/register.component';
import { ReportComponent } from './features/report.component/report.component';
import { IssueComponent } from './features/issue.component/issue.component';
import { ProjectComponent } from './features/project.component/project.component';

const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

export const routes: Routes = [
  {
    path: 'register',
    component: RegisterComponent,
    title: 'BugBoard26 - Register',
  },
  {
    path: 'login',
    component: LoginComponent,
    title: 'BugBoard26 - Login',
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    title: 'BugBoard26 - Dashboard',
    canActivate: [authGuard],
  },
  {
    path: 'projects',
    component: ProjectComponent,
    title: 'BugBoard26 - Project',
    canActivate: [authGuard],
  },
  {
    path: 'reports/:projectId',
    component: ReportComponent,
    title: 'BugBoard26 - Report',
    canActivate: [authGuard],
  },
  {
    path: ':projectId/issues',
    component: IssueComponent,
    title: 'BugBoard26 - Issue',
    canActivate: [authGuard],
  },
  {
    path: 'test-issues',
    component: IssueComponent,
    title: 'BugBoard26 - Issue',
  },
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full',
  },
];
