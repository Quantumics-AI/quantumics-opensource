import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { NgbModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { NgSelectModule } from '@ng-select/ng-select';
import { NgCircleProgressModule } from 'ng-circle-progress';
import { RouterModule } from '@angular/router';
import { TrialNotificationComponent } from './components/trial-notification/trial-notification.component';
import { NotificationComponent } from './components/notification/notification.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { HighlightPipe } from './components/notification/pipes/highlight.pipe';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SystemStatusComponent } from './components/system-status/system-status.component';
import { VerticalMenuComponent } from './components/vertical-menu/vertical-menu.component';
import { HorizontalMenuComponent } from './components/horizontal-menu/horizontal-menu.component';

@NgModule({
  declarations: [
    TrialNotificationComponent,
    NotificationComponent,
    UserProfileComponent,
    HighlightPipe,
    SystemStatusComponent,
    VerticalMenuComponent,
    HorizontalMenuComponent
  ],
  imports: [
    CommonModule,
    NgbModule,
    NgbTooltipModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    NgSelectModule,
    NgCircleProgressModule.forRoot({
      maxPercent: 50,
      backgroundOpacity: 0.7,
      backgroundPadding: 7,
      radius: 20,
      space: -2,
      outerStrokeWidth: 2,
      outerStrokeColor: '#808080',
      innerStrokeColor: '#e7e8ea',
      innerStrokeWidth: 2,
      animation: false,
      animateTitle: false,
      showTitle: true,
      showSubtitle: false,
      showUnits: true,
      showBackground: false,
      clockwise: false,
      startFromZero: true,
      titleFontSize: '12',
    })
  ],
  providers: [
    DatePipe
  ],
  exports: [
    VerticalMenuComponent,
    HorizontalMenuComponent,
    TrialNotificationComponent
  ]
})
export class CoreModule { }
