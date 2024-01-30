import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AutomationComponent } from './automation.component';
import { AutoRunSankeyComponent } from './components/auto-run-sankey/auto-run-sankey.component';
import { CleansingJobsComponent } from './components/cleansing-jobs/cleansing-jobs.component';
import { EngineeringJobsComponent } from './components/engineering-jobs/engineering-jobs.component';
import { JobsLogsComponent } from './components/jobs-logs/jobs-logs.component';
import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { EngineeringHistoryComponent } from './components/engineering-history/engineering-history.component';
import { CleansingJobHistoryComponent } from './components/cleansing-job-history/cleansing-job-history.component';

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
        component: CleansingJobsComponent
      },
      {
        path: 'cleansing/history/:jobId/:jobName',
        component: CleansingJobHistoryComponent
      },
      {
        path: 'engineering',
        component: EngineeringJobsComponent
      },
      {
        path: 'engineering/dataset',
        component: DatasetListComponent
      },
      {
        path: 'engineering/history',
        component: EngineeringHistoryComponent
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
