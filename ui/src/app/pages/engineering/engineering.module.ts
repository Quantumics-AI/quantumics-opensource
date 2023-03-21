import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgbNavModule, NgbAccordionModule, NgbTooltipModule, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';

import { EngineeringRoutingModule } from './engineering-routing.module';
import { EngineeringComponent } from './engineering.component';
import { ListComponent } from './components/list/list.component';
import { AddComponent } from './components/add/add.component';
import { EditComponent } from './components/edit/edit.component';
import { FoldersComponent } from './components/folders/folders.component';
import { PreviewComponent } from './components/preview/preview.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToolbarComponent } from './components/toolbar/toolbar.component';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { ConfirmationDialogModule } from '../../core/components/confirmation-dialog/confirmation-dialog.module';
import { PreviewFileComponent } from './components/preview-file/preview-file.component';
import { PreviewJoinComponent } from './components/preview-join/preview-join.component';
import { PreviewAggregateComponent } from './components/preview-aggregate/preview-aggregate.component';
import { FilterPipeModule } from 'ngx-filter-pipe';
import { FormatPipe } from './pipe/format.pipe';
import { PreviewUdfComponent } from './components/preview-udf/preview-udf.component';
import { EngineerContainerComponent } from './components/engineer-container/engineer-container.component';
import { UdfContainerComponent } from './components/udf-container/udf-container.component';
import { UdfListComponent } from './components/udf-list/udf-list.component';
import { CreateUpdateUdfComponent } from './components/create-update-udf/create-update-udf.component';
import { EditFlowComponent } from './components/edit-flow/edit-flow.component';

@NgModule({
  declarations: [
    EngineeringComponent,
    ListComponent,
    AddComponent,
    FoldersComponent,
    PreviewComponent,
    EditComponent,
    ToolbarComponent,
    PreviewFileComponent,
    PreviewJoinComponent,
    PreviewAggregateComponent,
    FormatPipe,
    PreviewUdfComponent,
    EngineerContainerComponent,
    UdfContainerComponent,
    UdfListComponent,
    CreateUpdateUdfComponent,
    EditFlowComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgbNavModule,
    NgbAccordionModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    DragDropModule,
    EngineeringRoutingModule,
    LoaderModule,
    ConfirmationDialogModule,
    NgbTooltipModule,
    FilterPipeModule,
    NgbPopoverModule
  ]
})
export class EngineeringModule { }
