import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectsRoutingModule } from './projects-routing.module';
import { ProjectsComponent } from './projects.component';
import { NgbModule, NgbDropdownModule, NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { AddComponent } from './components/add/add.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { ConfirmationDialogComponent } from './components/confirmation-dialog/confirmation-dialog.component';
import { SuccessComponent } from './components/success/success.component';
import { CancelledComponent } from './components/cancelled/cancelled.component';
import { InformationComponent } from './components/information/information.component';
import { InvoicesComponent } from './components/invoices/invoices.component';
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { AuditFilterPipe } from './pipes/audit-filter.pipe';
import { ProjectGridViewComponent } from './components/project-grid-view/project-grid-view.component';
import { ProjectListViewComponent } from './components/project-list-view/project-list-view.component';
import { InteractiveDemoComponent } from './components/interactive-demo/interactive-demo.component';
import { EngineeringUdfComponent } from './components/engineering-udf/engineering-udf.component';
import { CreateUdfComponent } from './components/create-udf/create-udf.component';
import { EditUdfComponent } from './components/edit-udf/edit-udf.component';
import { UpdateUdfComponent } from './components/update-udf/update-udf.component';
import { ProjectComponent } from './components/project/project.component';
import { ProjectRestoredComponent } from './components/project-restored/project-restored.component';

@NgModule({
  declarations: [
    ProjectsComponent,
    AddComponent,
    ConfirmationDialogComponent,
    SuccessComponent,
    CancelledComponent,
    InformationComponent,
    InvoicesComponent,
    SafeHtmlPipe,
    AuditFilterPipe,
    ProjectGridViewComponent,
    ProjectListViewComponent,
    InteractiveDemoComponent,
    EngineeringUdfComponent,
    CreateUdfComponent,
    EditUdfComponent,
    UpdateUdfComponent,
    ProjectComponent,
    ProjectRestoredComponent,
  ],
  imports: [
    NgbModule,
    CommonModule,
    ProjectsRoutingModule,
    NgbNavModule,
    NgbTooltipModule,
    FormsModule,
    ReactiveFormsModule,
    LoaderModule,
    NgbDropdownModule
  ]
})
export class ProjectsModule { }
