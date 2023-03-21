import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { interval, Observable, Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { NotificationService } from '../notification/services/notification.service';

declare let window: any;

@Component({
  selector: 'app-horizontal-menu',
  templateUrl: './horizontal-menu.component.html',
  styleUrls: ['./horizontal-menu.component.scss']
})
export class HorizontalMenuComponent implements OnInit {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private subscription: Subscription;

  public projectId: number;
  state$: Observable<any>;
  userName: string;
  userImageUrl: string;
  isSuperAdmin: boolean;
  notificationCount: number;
  userId: number;
  public userEmail: string;

  constructor(
    private quantumFacade: Quantumfacade,
    private router: Router,
    private notificationService: NotificationService) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.userName = certificate.user.split(' ')[0];
          this.userEmail = certificate.email;
          this.userId = +certificate.user_id;
          this.userImageUrl = certificate.userImageUrl;
          this.isSuperAdmin = certificate.userRole.toLowerCase() === 'admin';
        }
      });

    this.getIntervalNotification();
  }

  ngOnInit(): void {
    this.projectId = +localStorage.getItem('project_id');
  }

  public logout() {

    const email = localStorage.getItem('email');
    localStorage.clear();

    if (email) {
      localStorage.setItem('email', email);
    }

    this.router.navigate(['/login']);
  }

  private getIntervalNotification(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    const source = interval(15000);
    this.subscription = source.subscribe(() => this.getNotificationCount());
  }

  private getNotificationCount(): void {
    this.projectId = +localStorage.getItem('project_id');

    if (this.projectId) {
      this.notificationService.getNotification(this.projectId, this.userId, 'count').subscribe((response) => {
        if(response.result == 0){
          this.notificationCount = 0;
        } else {
          this.notificationCount = response.result;
        }
      }, () => {
        this.notificationCount = 0;
      });
    }
  }
  
}
