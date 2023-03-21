import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { NgSelectModule } from '@ng-select/ng-select';

// Routing
import { AccountSettingsRoutingModule } from './account-settings-routing.module';

import { NgbModule, NgbDropdownModule, NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Custom Modules
import { LoaderModule } from '../../core/components/loader/loader.module';

// Components
import { AccountSettingsComponent } from './account-settings.component';
import { ProfileComponent } from './components/profile/profile.component';
import { EmailModalComponent } from './components/email-modal/email-modal.component';
import { ReferralComponent } from './components/referral/referral.component';
import { CanDeactivateGuard } from 'src/app/core/guards/can-deactivate.guard';


@NgModule({
    declarations: [
        AccountSettingsComponent,
        ProfileComponent,
        EmailModalComponent,
        ReferralComponent,
    ],
    imports: [
        CommonModule,
        AccountSettingsRoutingModule,
        NgbTooltipModule,
        NgSelectModule,
        NgbModule,
        NgbDropdownModule,
        NgbNavModule,
        FormsModule,
        ReactiveFormsModule,
        LoaderModule
    ],
    providers: [
        CanDeactivateGuard
    ]
})
export class AccountSettingsModule { }
