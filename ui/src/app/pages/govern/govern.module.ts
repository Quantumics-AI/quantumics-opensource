import { NgModule } from '@angular/core';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TagInputModule } from 'ngx-chips';
import { NgbAccordionModule } from '@ng-bootstrap/ng-bootstrap';
import { GoogleChartsModule } from 'angular-google-charts';




import { DatePipe } from '@angular/common';
import { FilterPipe } from './pipes/filter.pipe';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { NgSelectModule } from '@ng-select/ng-select';
import { GovernRoutingModule } from './govern-routing.module';

import { GovernComponent } from './govern.component';
import { BusinessGlossaryComponent } from './components/business-glossary/business-glossary.component';
import { GlossaryListComponent } from './components/glossary-list/glossary-list.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { EditComponent } from './components/edit/edit.component';
import { DictionaryRuleComponent } from './components/dictionary-rule/dictionary-rule.component';
import { GoogleSankeyChartComponent } from './components/google-sankey-chart/google-sankey-chart.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [
    GovernComponent,
    BusinessGlossaryComponent,
    GlossaryListComponent,
    FilterPipe,
    DashboardComponent,
    EditComponent,
    DictionaryRuleComponent,
    GoogleSankeyChartComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    TagInputModule,
    ReactiveFormsModule,
    GovernRoutingModule,
    NgbAccordionModule,
    LoaderModule,
    NgSelectModule,
    GoogleChartsModule,
    SharedModule
  ],
  providers: [DatePipe, NgbActiveModal]
})
export class GovernModule { }
