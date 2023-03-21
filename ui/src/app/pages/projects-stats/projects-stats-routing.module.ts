import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ProjectsStatsComponent } from './projects-stats.component';


const routes: Routes = [
  {
    path: '',
    component: ProjectsStatsComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProjectsStatsRoutingModule { }
