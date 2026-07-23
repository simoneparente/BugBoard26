import { ActivatedRouteSnapshot, RouterStateSnapshot, Routes, Router } from '@angular/router';
import { inject } from '@angular/core';
import { CreateIssueComponent } from './features/create-issue.component/create-issue.component';
import { AuthService } from './core/auth/auth-service';
import { LoginComponent } from './features/login.component/login.component';

import { DashboardComponent } from './features/dashboard.component/dashboard.component';
import { RegisterComponent } from './features/register.component/register.component';
import { ProjectComponent } from './features/project.component/project.component';
import { ReportComponent } from './features/report.component/report.component';
import { TagManagementComponent } from './features/tag-management.component/tag-management.component';
import { IssueComponent } from './features/issue.component/issue.component';
import { LayoutComponent } from './layout/layout.component';
import { CreateProjectComponent } from './features/create-project.component/create-project.component';
import { IssueDetailComponent } from './features/issue-detail.component/issue-detail.component';

const authGuard = (_route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
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
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        component: DashboardComponent,
        title: 'BugBoard26 - Dashboard',
      },
      {
        path: 'projects',
        component: ProjectComponent,
        title: 'BugBoard26 - Projects',
      },
      {
        path: 'projects/:projectId/issues',
        component: IssueComponent,
        title: 'BugBoard26 - Issue',
        canActivate: [authGuard],
      },
      {
        path: 'projects/create',
        component: CreateProjectComponent,
        title: 'BugBoard26 - Create Project',
      },
      {
        path: 'projects/:projectId',
        redirectTo: 'projects/:projectId/issues',
        pathMatch: 'full',
      },
      {
        path: 'projects/:projectId/tags',
        component: TagManagementComponent,
        title: 'BugBoard26 - Tag Management',
      },
      {
        path: 'projects/:projectId/report',
        component: ReportComponent,
        title: 'BugBoard26 - Report',
      },
      {
        path: 'projects/:projectId/issues/create',
        component: CreateIssueComponent,
        title: 'BugBoard26 - Create Issue',
      },
      {
        path: 'projects/:projectId/issues/:issueId',
        component: IssueDetailComponent,
        title: 'BugBoard26 - Issue Details',
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },
];
