import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from './core/guards/auth-guard.service';

const routes: Routes = [
  {
    path: 'projects',
    loadChildren: () => import('./pages/projects/projects.module').then(m => m.ProjectsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'account',
    loadChildren: () => import('./pages/account-settings/account-settings.module').then(m => m.AccountSettingsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'workspaces',
    loadChildren: () => import('./pages/all-workspaces/all-workspaces.module').then(m => m.AllWorkspacesModule),
    canActivate: [AuthGuard],
    data: { hideNavigation: true }
  },
  {
    path: 'login',
    loadChildren: () => import('./pages/login/login.module').then(m => m.LoginModule),
    data: { hideNavigation: true }
  },
  {
    path: '**',
    redirectTo: 'projects',
    pathMatch: 'full'
  }
];

const routerOptions: any = {
  initialNavigation: 'enabled',
  relativeLinkResolution: 'legacy'
};

@NgModule({
  imports: [RouterModule.forRoot(routes, routerOptions)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
