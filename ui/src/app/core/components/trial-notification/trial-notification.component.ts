import { Component } from '@angular/core';
import { Certificate } from 'src/app/models/certificate';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SubscriptionService } from './services/subscription.service';
import { Subscription } from './models/subscription';

@Component({
  selector: 'app-trial-notification',
  templateUrl: './trial-notification.component.html',
  styleUrls: ['./trial-notification.component.scss']
})
export class TrialNotificationComponent {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  public close: boolean;
  public subscription: Subscription;

  constructor(
    private quantumFacade: Quantumfacade,
    private subscriptionService: SubscriptionService) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$.pipe(takeUntil(this.unsubscribe)).subscribe(certificate => {
      if (certificate) {
        const userId = +certificate.user_id;
        this.subscriptionService.getSubscriptionDetails(userId).subscribe((response) => {
          this.subscription = response.result;
        });
      }
    });
  }
}
