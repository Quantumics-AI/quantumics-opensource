import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SankeyChartComponent } from './sankey-chart/sankey-chart.component';



@NgModule({
  declarations: [SankeyChartComponent],
  imports: [
    CommonModule
  ],
  exports: [
    SankeyChartComponent
  ]
})
export class SharedModule { }
