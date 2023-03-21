import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AllWorkspacesComponent } from './all-workspaces.component';

const routes: Routes = [
    {
        path: '',
        component: AllWorkspacesComponent
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AllWorkSpaceRoutingModule { }
