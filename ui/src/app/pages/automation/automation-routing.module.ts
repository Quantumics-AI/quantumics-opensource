import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AutomationComponent } from './automation.component';
import { AutoRunSankeyComponent } from './components/auto-run-sankey/auto-run-sankey.component';
import { CleansingJobsComponent } from './components/cleansing-jobs/cleansing-jobs.component';
import { EngineeringJobsComponent } from './components/engineering-jobs/engineering-jobs.component';
import { JobsLogsComponent } from './components/jobs-logs/jobs-logs.component';

const routes: Routes = [
  {
    path: '',
    component: AutomationComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'cleansing'
      },
      {
        path: 'cleansing',
        component: CleansingJobsComponent,
      },
      {
        path: 'engineering',
        component: EngineeringJobsComponent,
      }
    ]
  },
  {
    path: ':id/s-autorun',
    component: AutoRunSankeyComponent
  },
  {
    path: 'logs',
    component: JobsLogsComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AutomationRoutingModule { }
