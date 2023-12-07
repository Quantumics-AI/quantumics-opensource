import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbDropdownModule, NgbPaginationModule, NgbPopoverModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { FilterPipeModule } from 'ngx-filter-pipe';
import { HttpClientModule } from '@angular/common/http';
import { GoogleChartsModule } from 'angular-google-charts';
import { SharedModule } from 'src/app/shared/shared.module';
import { NgSelectModule } from '@ng-select/ng-select';

import { LoaderModule } from '../../core/components/loader/loader.module';
import { AutomationRoutingModule } from './automation-routing.module';

import { ListRawDataComponent } from './components/list-raw-data/list-raw-data.component';
import { AutomationComponent } from './automation.component';
import { CleansingJobsComponent } from './components/cleansing-jobs/cleansing-jobs.component';
import { EngineeringJobsComponent } from './components/engineering-jobs/engineering-jobs.component';
import { EngineeringHistoryComponent } from './components/engineering-history/engineering-history.component';
import { SelectFilesComponent } from './components/select-files/select-files.component';
import { JobsLogsComponent } from './components/jobs-logs/jobs-logs.component';
import { AutoRunSankeyComponent } from './components/auto-run-sankey/auto-run-sankey.component';
import { CleansingJobHistoryComponent } from './components/cleansing-job-history/cleansing-job-history.component';


import { DatasetListComponent } from './components/dataset-list/dataset-list.component';

@NgModule({
  declarations: [
    AutomationComponent,
    CleansingJobsComponent,
    EngineeringJobsComponent,
    EngineeringHistoryComponent,
    SelectFilesComponent,
    JobsLogsComponent,
    AutoRunSankeyComponent,
    ListRawDataComponent,
    CleansingJobHistoryComponent,
    DatasetListComponent
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
    SharedModule,
    NgbPopoverModule,
    NgbPaginationModule,
    NgbDropdownModule,
    NgSelectModule,
    NgbPopoverModule,
    NgbPaginationModule,
    NgbDropdownModule

  ]
})
export class AutomationModule { }
