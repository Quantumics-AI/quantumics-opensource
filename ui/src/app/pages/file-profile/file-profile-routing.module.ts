import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
// import { DataQualityComponent } from './components/data-quality/data-quality.component';
import { ViewFileProfileComponent } from './components/view-file-profile/view-file-profile.component';

import { FileProfileComponent } from './file-profile.component';

const routes: Routes = [
  {
    path: '',
    component: FileProfileComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'data-profile'
      },
      {
        path: 'data-profile',
        component: ViewFileProfileComponent,
      },
      // {
      //   path: 'data-quality',
      //   component: DataQualityComponent,
      // },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FileProfileRoutingModule { }
