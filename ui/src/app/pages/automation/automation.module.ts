import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

import { AutomationRoutingModule } from './automation-routing.module';
import { FilterPipeModule } from 'ngx-filter-pipe';

import { AutomationComponent } from './automation.component';
import { CleansingJobsComponent } from './components/cleansing-jobs/cleansing-jobs.component';
import { EngineeringJobsComponent } from './components/engineering-jobs/engineering-jobs.component';
import { CleansingHistoryComponent } from './components/cleansing-history/cleansing-history.component';
import { EngineeringHistoryComponent } from './components/engineering-history/engineering-history.component';
import { HttpClientModule } from '@angular/common/http';
import { SelectFilesComponent } from './components/select-files/select-files.component';
import { CleansingSelectFilesComponent } from './components/cleansing-select-files/cleansing-select-files.component';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { JobsLogsComponent } from './components/jobs-logs/jobs-logs.component';
import { AutoRunSankeyComponent } from './components/auto-run-sankey/auto-run-sankey.component';
import { GoogleChartsModule } from 'angular-google-charts';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [
    AutomationComponent,
    CleansingJobsComponent,
    EngineeringJobsComponent,
    CleansingHistoryComponent,
    EngineeringHistoryComponent,
    SelectFilesComponent,
    CleansingSelectFilesComponent,
    JobsLogsComponent,
    AutoRunSankeyComponent,
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    AutomationRoutingModule,
    LoaderModule,
    NgbTooltipModule,
    FormsModule,
    FilterPipeModule,
    GoogleChartsModule,
    SharedModule
  ]
})
export class AutomationModule { }
