import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { EditComponent } from './components/edit/edit.component';
import { EngineeringComponent } from './engineering.component';
import { EngineerContainerComponent } from './components/engineer-container/engineer-container.component';
import { UdfContainerComponent } from './components/udf-container/udf-container.component';


const routes: Routes = [
  {
    path: '',
    component: EngineeringComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'engineer'
      },
      {
        path: 'engineer',
        component: EngineerContainerComponent,
      },
      {
        path: 'udf',
        component: UdfContainerComponent,
      }
    ]
  },
  {
    path: 'engineer/:flowId/edit/:flowName',
    component: EditComponent
  },
  {
    path: 'engineer/:flowId/view/:flowName',
    component: EditComponent
  },
  {
    path: '',
    component: EngineeringComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EngineeringRoutingModule { }
