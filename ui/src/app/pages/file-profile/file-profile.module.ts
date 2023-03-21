import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbNavModule, NgbTooltipModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { NgSelectModule } from '@ng-select/ng-select';

import { FileProfileRoutingModule } from './file-profile-routing.module';
import { LoaderModule } from 'src/app/core/components/loader/loader.module';
// import { LockModule } from 'src/app/core/components/lock/lock.module';


import { FileProfileComponent } from './file-profile.component';
import { ViewFileProfileComponent } from './components/view-file-profile/view-file-profile.component';
import { DataQualityComponent } from './components/data-quality/data-quality.component';
import { FilterColumnPipe, FilterPipe } from './pipes/filter.pipe';
import { InvalidDataComponent } from './components/invalid-data/invalid-data.component';
import { ColumnsComponent } from './components/columns/columns.component';
import { FrequencyAnalysisComponent } from './components/frequency-analysis/frequency-analysis.component';
import { DataAnalysisComponent } from './components/data-analysis/data-analysis.component';
import { StatisticalAnalysisComponent } from './components/statistical-analysis/statistical-analysis.component';
import { QualityComponent } from './components/quality/quality.component';
import { ViewDatasetComponent } from './components/view-dataset/view-dataset.component';


@NgModule({
  declarations: [
    FileProfileComponent,
    ViewFileProfileComponent,
    DataQualityComponent,
    FilterPipe,
    FilterColumnPipe,
    InvalidDataComponent,
    ColumnsComponent,
    FrequencyAnalysisComponent,
    DataAnalysisComponent,
    StatisticalAnalysisComponent,
    QualityComponent,
    ViewDatasetComponent
  ],
  imports: [
    CommonModule,
    FileProfileRoutingModule,
    ReactiveFormsModule,
    NgbNavModule,
    NgbTooltipModule,
    LoaderModule,
    FormsModule,
    NgSelectModule,
    // LockModule,
    NgbDropdownModule
  ],
  schemas: [NO_ERRORS_SCHEMA]
})
export class FileProfileModule { }
