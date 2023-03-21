import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ProjectValidityGuard } from 'src/app/core/guards/project-validity.guard';
import { RoleGuard } from 'src/app/core/guards/role.guard';
import { CancelledComponent } from './components/cancelled/cancelled.component';
import { SuccessComponent } from './components/success/success.component';
import { EngineeringUdfComponent } from './components/engineering-udf/engineering-udf.component';
import { CreateUdfComponent } from './components/create-udf/create-udf.component'
import { EditUdfComponent } from './components/edit-udf/edit-udf.component'

import { ProjectsComponent } from './projects.component';
import { ProjectRestoredComponent } from './components/project-restored/project-restored.component';
import { ExpiredComponent } from './components/expired/expired.component';

const routes: Routes = [
  {
    path: '',
    component: ProjectsComponent
  },
  {
    path: ':projectId',
    canActivate: [ProjectValidityGuard],
    children: [
      {
        path: 'expired',
        component: ExpiredComponent
      },
      {
        path: 'restored',
        component: ProjectRestoredComponent
      },
      {
        path: 'subscription-success',
        component: SuccessComponent
      },
      {
        path: 'subscription-cancelled',
        component: CancelledComponent
      },
      {
        path: 'engineering-udf',
        component: EngineeringUdfComponent,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'create-udf'
          },
          {
            path: 'create-udf',
            component: CreateUdfComponent
          },
          {
            path: 'edit-udf',
            component: EditUdfComponent
          }
        ]
      },
      {
        path: 'cleansing',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../cleansing/cleansing.module').then(m => m.CleansingModule)
      },
      {
        path: 'engineering',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../engineering/engineering.module').then(m => m.EngineeringModule)
      },
      {
        path: 'ingest',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../ingest/ingest.module').then(m => m.IngestModule)
      },
      {
        path: 'automation',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../automation/automation.module').then(m => m.AutomationModule)
      },
      {
        path: 'apps',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../apps/apps.module').then(m => m.AppsModule)
      },
      {
        path: 'stats',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../projects-stats/projects-stats.module').then(m => m.ProjectsStatsModule)
      },
      {
        path: 'delta',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../file-profile/file-profile.module').then(m => m.FileProfileModule)
      },
      {
        path: 'workspace-setting',
        canActivate: [ProjectValidityGuard],
        loadChildren: () => import('../workspace-settings/workspace-settings.module').then(m => m.WorkspaceSettingsModule)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProjectsRoutingModule { }
