import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BusinessGlossaryComponent } from './components/business-glossary/business-glossary.component';
import { GlossaryListComponent } from './components/glossary-list/glossary-list.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { GovernComponent } from './govern.component';
import { GoogleSankeyChartComponent } from './components/google-sankey-chart/google-sankey-chart.component';

const routes: Routes = [
  {
    path: 'dashboard',
    component: DashboardComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'list'
      },
      {
        path: 'list',
        component: GlossaryListComponent
      },
      {
        path: 'glossary',
        component: BusinessGlossaryComponent
      },
      {
        path: ':id/glossary',
        component: BusinessGlossaryComponent
      },
      {
        path: ':id/schart',
        component: GoogleSankeyChartComponent
      }
    ]
  },
  {
    path: ':id/schart',
    component: GoogleSankeyChartComponent

  },
  {
    path: '**',
    component: GovernComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GovernRoutingModule { }
