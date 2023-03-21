import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CleansingComponent } from './cleansing.component';
import { AddCleansingComponent } from './components/add-cleansing/add-cleansing.component';

const routes: Routes = [
  { path: '', component: AddCleansingComponent },
  { path: 'action', component: CleansingComponent },
  { path: ':folderId/:fileId', component: CleansingComponent },

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CleansingRoutingModule { }
