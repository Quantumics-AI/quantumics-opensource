import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CleansingComponent } from './cleansing.component';
import { AddCleansingComponent } from './components/add-cleansing/add-cleansing.component';
import { CleansedFilesComponent } from './components/cleansed-files/cleansed-files.component';

const routes: Routes = [
  {
    path: '',
    component: AddCleansingComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'cleansing'
      },

    ]
  },
  {
    path: 'jobs',
    component: CleansedFilesComponent,
  },
  { path: 'action', component: CleansingComponent },
  { path: ':folderId/:fileId', component: CleansingComponent },

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CleansingRoutingModule { }
