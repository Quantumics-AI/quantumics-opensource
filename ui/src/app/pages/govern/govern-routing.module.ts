import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BusinessGlossaryComponent } from './components/business-glossary/business-glossary.component';
import { GlossaryListComponent } from './components/glossary-list/glossary-list.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { GovernComponent } from './govern.component';
import { GoogleSankeyChartComponent } from './components/google-sankey-chart/google-sankey-chart.component';
import { EditComponent } from './components/edit/edit.component';
import { DictionaryRuleComponent } from './components/dictionary-rule/dictionary-rule.component';

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
        path: 'list/glossary',
        component: BusinessGlossaryComponent
      },
      {
        path: ':id/glossary',
        component: BusinessGlossaryComponent
      },
      {
        path: ':id/schart',
        component: GoogleSankeyChartComponent
      },
      {
        path: 'list/update-glosssary',
        component: EditComponent,
      },
      {
        path: 'schart',
        component: GoogleSankeyChartComponent,
      },
      {
        path: 'data-quality/dictionary-rule',
        component: DictionaryRuleComponent,
      },
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
