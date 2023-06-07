import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ProjectsStatsRoutingModule } from './projects-stats-routing.module';
import { ProjectsStatsComponent } from './projects-stats.component';
import { ChartsModule } from 'ng2-charts';
import { TopBarComponent } from './top-bar/top-bar.component';
import { RightSideComponent } from './right-side/right-side.component';
import { BottomSideComponent } from './bottom-side/bottom-side.component';
import { BarChartComponent } from './bar-chart/bar-chart.component';
import { SampleDatasetComponent } from './sample-dataset/sample-dataset.component';
import { NgbModule, NgbDropdownModule, NgbNavModule, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { LineChartComponent } from './line-chart/line-chart.component';
import { LoaderModule } from 'src/app/core/components/loader/loader.module';


@NgModule({
  declarations: [
    ProjectsStatsComponent,
    TopBarComponent,
    RightSideComponent,
    BottomSideComponent,
    BarChartComponent,
    SampleDatasetComponent,
    LineChartComponent
  ],
  imports: [
    CommonModule,
    ProjectsStatsRoutingModule,
    ChartsModule,
    NgbPopoverModule,
    NgbModule,
    NgbDropdownModule,
    NgbNavModule,
    LoaderModule
  ]
})
export class ProjectsStatsModule { }
