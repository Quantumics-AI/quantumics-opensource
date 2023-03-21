import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingsComponent } from './settings.component';
import { SettingsRoutingModule } from './settings-routing.module';
import { MyPlanComponent } from './components/my-plan/my-plan.component';
import { PaymentInformationComponent } from './components/payment-information/payment-information.component';
import { InvoiceHistoryComponent } from './components/invoice-history/invoice-history.component';
import { ChangePlanComponent } from './components/change-plan/change-plan.component';



@NgModule({
  declarations: [SettingsComponent, MyPlanComponent, PaymentInformationComponent, InvoiceHistoryComponent, ChangePlanComponent],
  imports: [
    CommonModule,
    SettingsRoutingModule
  ]
})
export class SettingsModule { }
