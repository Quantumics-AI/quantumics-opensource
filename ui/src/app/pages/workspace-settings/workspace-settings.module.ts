import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WorkspaceSettingsRoutingModule } from './workspace-settings-routing.module';
import { WorkspaceDetailsComponent } from './components/workspace-details/workspace-details.component';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmationModalComponent } from './components/confirmation-modal/confirmation-modal.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { NgbModule, NgbDropdownModule, NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { AuditFilterPipe } from './pipes/audit-filter.pipe';
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { CanDeactivateGuard } from 'src/app/core/guards/can-deactivate.guard';
import { ConfirmationDialogModule } from 'src/app/core/components/confirmation-dialog/confirmation-dialog.module';

@NgModule({
  declarations: [
    WorkspaceDetailsComponent,
    ConfirmationModalComponent,
    AuditFilterPipe,
    SafeHtmlPipe,
  ],
  imports: [
    CommonModule,
    WorkspaceSettingsRoutingModule,
    NgbTooltipModule,
    NgSelectModule,
    NgbModule,
    NgbDropdownModule,
    NgbNavModule,
    FormsModule,
    ReactiveFormsModule,
    LoaderModule,
    ConfirmationDialogModule
  ],
  providers: [
    CanDeactivateGuard
  ]
})
export class WorkspaceSettingsModule { }
