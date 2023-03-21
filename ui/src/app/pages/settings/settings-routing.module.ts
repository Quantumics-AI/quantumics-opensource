import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { InvoiceHistoryComponent } from './components/invoice-history/invoice-history.component';
import { MyPlanComponent } from './components/my-plan/my-plan.component';
import { ChangePlanComponent } from './components/change-plan/change-plan.component';
import { PaymentInformationComponent } from './components/payment-information/payment-information.component';
import { SettingsComponent } from './settings.component';


const routes: Routes = [
  {
    path: '',
    component: SettingsComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'my-plan'
      },
      {
        path: 'my-plan',
        component: MyPlanComponent,
      },
      {
        path: 'change-plan',
        component: ChangePlanComponent,
      },
      {
        path: 'payment-information',
        component: PaymentInformationComponent,
      },
      {
        path: 'invoice-history',
        component: InvoiceHistoryComponent,
      },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule { }
