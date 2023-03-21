import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AccountSettingsComponent } from './account-settings.component';
import { ProfileComponent } from './components/profile/profile.component';
import { ReferralComponent } from './components/referral/referral.component';
import { CanDeactivateGuard } from 'src/app/core/guards/can-deactivate.guard';

const routes: Routes = [
    {
        path: '',
        component: AccountSettingsComponent,
        children: [
            {
                path: '',
                pathMatch: 'full',
                redirectTo: 'profile'
            },
            {
                path: 'profile',
                component: ProfileComponent,
                canDeactivate: [CanDeactivateGuard]
            },
            {
                path: 'ref',
                component: ReferralComponent
            },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AccountSettingsRoutingModule { }
