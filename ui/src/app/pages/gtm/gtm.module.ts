import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { GtmRoutingModule } from './gtm-routing.module';
import { GtmComponent } from './gtm.component';

@NgModule({
  declarations: [GtmComponent],
  imports: [
    CommonModule,
    GtmRoutingModule
  ]
})
export class GtmModule { }
