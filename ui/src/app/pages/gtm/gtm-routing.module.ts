import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GtmComponent } from './gtm.component';


const routes: Routes = [
  {
    path: '**',
    component: GtmComponent
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GtmRoutingModule { }
