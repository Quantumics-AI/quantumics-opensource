import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ProjectsStatsRoutingModule } from './projects-stats-routing.module';
import { ProjectsStatsComponent } from './projects-stats.component';
import { ChartsModule } from 'ng2-charts';
import { TopBarComponent } from './top-bar/top-bar.component';
import { RightSideComponent } from './right-side/right-side.component';
import { BottomSideComponent } from './bottom-side/bottom-side.component';
import { BarChartComponent } from './bar-chart/bar-chart.component';


@NgModule({
  declarations: [
    ProjectsStatsComponent,
    TopBarComponent,
    RightSideComponent,
    BottomSideComponent,
    BarChartComponent
  ],
  imports: [
    CommonModule,
    ProjectsStatsRoutingModule,
    ChartsModule
  ]
})
export class ProjectsStatsModule { }
