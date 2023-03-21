import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbModule, NgbNavModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { FilterPipeModule } from 'ngx-filter-pipe';

import { FilterDatabasePipe, FilterPipe } from './pipes/filter.pipe';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { IngestRoutingModule } from './ingest-routing.module';

import { IngestComponent } from './ingest.component';
import { AddFolderComponent } from './components/add-folder/add-folder.component';
import { ListFoldersComponent } from './components/list-folders/list-folders.component';
import { ConnectDbComponent } from './components/connect-db/connect-db.component';
import { ConnectSuccessComponent } from './components/connect-success/connect-success.component';
import { SelectSourceTypeComponent } from './components/select-source-type/select-source-type.component';
import { ConnectToApiComponent } from './components/connect-to-api/connect-to-api.component';
import { ImportLocalFileComponent } from './components/import-local-file/import-local-file.component';
import { SourceDataComponent } from './components/source-data/source-data.component';
import { ListSourceDataComponent } from './components/list-source-data/list-source-data.component';
import { PiiComponent } from './components/pii/pii.component';
import { OutliersComponent } from './components/outliers/outliers.component';
import { ViewIngestComponent } from './components/view-ingest/view-ingest.component';
import { FileStatisticsComponent } from './components/file-statistics/file-statistics.component';
import { DeltaComponent } from './components/delta/delta.component';
import { EditFolderComponent } from './components/edit-folder/edit-folder.component';
import { CreateFolderComponent } from './components/create-folder/create-folder.component';
import { WizardFolderComponent } from './components/wizard-folder/wizard-folder.component';
import { ConfigureFolderComponent } from './components/configure-folder/configure-folder.component';
import { CompleteFolderCreationComponent } from './components/complete-folder-creation/complete-folder-creation.component';
import { PiiInfoComponent } from './components/pii-info/pii-info.component';
import { DbConnectorComponent } from './components/db-connector/db-connector.component';
import { DbConnectionComponent } from './components/db-connection/db-connection.component';
import { CreateDbPipelineComponent } from './components/create-db-pipeline/create-db-pipeline.component';
import { SelectDbTableComponent } from './components/select-db-table/select-db-table.component';
import { ListPipelinesComponent } from './components/list-pipelines/list-pipelines.component';
import { UpdatePipelineComponent } from './components/update-pipeline/update-pipeline.component';
import { ImportDbComponent } from './components/import-db/import-db.component';
import { PiiIdentificationComponent } from './components/pii-identification/pii-identification.component';
import { PipelineHistoryComponent } from './components/pipeline-history/pipeline-history.component';

@NgModule({
  declarations: [
    IngestComponent,
    AddFolderComponent,
    ListFoldersComponent,
    ConnectDbComponent,
    ConnectSuccessComponent,
    SelectSourceTypeComponent,
    FilterPipe,
    FilterDatabasePipe,
    ConnectToApiComponent,
    ImportLocalFileComponent,
    SourceDataComponent,
    ListSourceDataComponent,
    PiiComponent,
    OutliersComponent,
    ViewIngestComponent,
    FileStatisticsComponent,
    DeltaComponent,
    EditFolderComponent,
    CreateFolderComponent,
    WizardFolderComponent,
    ConfigureFolderComponent,
    CompleteFolderCreationComponent,
    PiiInfoComponent,
    DbConnectorComponent,
    DbConnectionComponent,
    CreateDbPipelineComponent,
    SelectDbTableComponent,
    ListPipelinesComponent,
    UpdatePipelineComponent,
    ImportDbComponent,
    PiiIdentificationComponent,
    PipelineHistoryComponent,
  ],
  imports: [
    CommonModule,
    IngestRoutingModule,
    NgbNavModule,
    NgbTooltipModule,
    FormsModule,
    ReactiveFormsModule,
    NgSelectModule,
    LoaderModule,
    FilterPipeModule,
    NgbModule
  ],
  schemas: [NO_ERRORS_SCHEMA]
})
export class IngestModule { }
