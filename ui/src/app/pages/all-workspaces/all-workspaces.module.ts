import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { FilterPipeModule } from 'ngx-filter-pipe';

import { HttpClientModule } from '@angular/common/http';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { AllWorkSpaceRoutingModule } from './all-workspaces-routing.module';
import { AllWorkspacesComponent } from './all-workspaces.component';
import { FilterPipe } from './pipes/filter.pipe';
import { RenameWorkspaceComponent } from './components/rename-workspace/rename-workspace.component';

@NgModule({
    declarations: [
        AllWorkspacesComponent,
        FilterPipe,
        RenameWorkspaceComponent
    ],
    imports: [
        CommonModule,
        HttpClientModule,
        AllWorkSpaceRoutingModule,
        LoaderModule,
        NgbTooltipModule,
        NgbDropdownModule,
        FormsModule,
        FilterPipeModule,
        SharedModule,
        ReactiveFormsModule
    ]
})
export class AllWorkspacesModule { }
