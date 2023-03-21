import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { WorkspaceSettingsComponent } from './workspace-settings.component';
import { WorkspaceDetailsComponent } from './components/workspace-details/workspace-details.component'
import { CanDeactivateGuard } from 'src/app/core/guards/can-deactivate.guard';

const routes: Routes = [
  {
    path: '',
    component: WorkspaceSettingsComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo:'details'
      },
      {
        path: 'details',
        component: WorkspaceDetailsComponent,
        canDeactivate: [CanDeactivateGuard]
      },
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class WorkspaceSettingsRoutingModule { }
