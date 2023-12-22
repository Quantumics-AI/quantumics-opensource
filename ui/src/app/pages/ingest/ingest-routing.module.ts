import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SourceDataComponent } from './components/source-data/source-data.component';
import { SelectSourceTypeComponent } from './components/select-source-type/select-source-type.component';
import { IngestComponent } from './ingest.component';
import { ViewIngestComponent } from './components/view-ingest/view-ingest.component';
import { WizardFolderComponent } from './components/wizard-folder/wizard-folder.component';
import { DbConnectorComponent } from './components/db-connector/db-connector.component';
import { ListFoldersComponent } from './components/list-folders/list-folders.component';
import { ListPipelinesComponent } from './components/list-pipelines/list-pipelines.component';
import { DbConnectionComponent } from './components/db-connection/db-connection.component';
import { CreateDbPipelineComponent } from './components/create-db-pipeline/create-db-pipeline.component';
import { SelectDbTableComponent } from './components/select-db-table/select-db-table.component';
import { PiiIdentificationComponent } from './components/pii-identification/pii-identification.component';
import { CompleteFolderCreationComponent } from './components/complete-folder-creation/complete-folder-creation.component';
import { ConfigureFolderComponent } from './components/configure-folder/configure-folder.component';
import { CreateFolderComponent } from './components/create-folder/create-folder.component';
import { PiiIdentificationDatabaseComponent } from './components/pii-identification-database/pii-identification-database.component';
import { CompleteDatabaseCreationComponent } from './components/complete-database-creation/complete-database-creation.component';
import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { FolderDatasetComponent } from './components/folder-dataset/folder-dataset.component';
import { ViewIngestPipelineComponent } from './components/view-ingest-pipeline/view-ingest-pipeline.component';
import { CreateAwsComponent } from './components/create-aws/create-aws.component';
import { ListAwsComponent } from './components/list-aws/list-aws.component';
import { SelectSourceTargetComponent } from './components/select-source-target/select-source-target.component';

const routes: Routes = [
  {
    path: '',
    component: IngestComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'folders'
      },
      {
        path: 'folders',
        component: ListFoldersComponent,
      },
      {
        path: 'pipelines',
        component: ListPipelinesComponent
      },
      {
        path: 'aws',
        component: ListAwsComponent
      },
      { 
        path: 'pipelines/dataset/:pipelineId/:folderId',
        component: DatasetListComponent
      },
      { 
        path: 'folders/dataset',
        component: FolderDatasetComponent
      },
      { 
        path: 'folders/view', 
        component: ViewIngestComponent 
      },
      { 
        path: 'pipelines/view', 
        component: ViewIngestPipelineComponent 
      },
    ]

  },
  { path: 'source-data', component: SourceDataComponent },
  { path: 'view', component: ViewIngestComponent },
  { path: 'select-source-type', component: SelectSourceTypeComponent },
  { path: 'generate-aws', component: CreateAwsComponent },
  { path: 'select-source-target', component: SelectSourceTargetComponent},
  {
    path: '',
    component: WizardFolderComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'local-file'
      },
      {
        path: 'local-file',
        component: CreateFolderComponent,
      },
      {
        path: 'configure-folder',
        component: ConfigureFolderComponent,
      },
      {
        path: 'pii-identification/:sourceType',
        component: PiiIdentificationComponent,
      },
      {
        path: 'completion/:sourceType',
        component: CompleteFolderCreationComponent,
      }
    ]
  },
  {
    path: '',
    component: DbConnectorComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'db-connector'
      },
      {
        path: 'db-connector',
        component: DbConnectionComponent,
      },
      {
        path: 'configure-pipeline',
        component: CreateDbPipelineComponent,
      },
      {
        path: 'select-table',
        component: SelectDbTableComponent,
      },
      {
        path: 'pii-identification-database/:sourceType',
        component: PiiIdentificationDatabaseComponent,
      },
      {
        path: 'database-completion/:sourceType',
        component: CompleteDatabaseCreationComponent,
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IngestRoutingModule { }
