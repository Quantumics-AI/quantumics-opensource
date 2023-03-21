import { DatePipe } from '@angular/common';
import { Component, OnInit} from '@angular/core';
import * as _ from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from '../../services/snackbar.service';
import { notification } from './models/notification';
import { NotificationService } from './services/notification.service';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  private userId: number;
  private projectId: number;
  public activeTab: string;
  public allNotification: Array<notification>;
  public unReadNotification: Array<notification>;
  public allNotificationGrp: any = [];
  public loading: boolean;

  constructor(
    private notificationService: NotificationService,
    private quantumFacade: Quantumfacade,
    private snakbar: SnackbarService,
    private datePipe: DatePipe) {
    this.activeTab = 'unreadNotification';
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$.pipe(takeUntil(this.unsubscribe)).subscribe(certificate => {
      if (certificate) {
        this.userId = +certificate.user_id;
      }
    });

    this.projectId = +localStorage.getItem('project_id');
  }

  ngOnInit(): void {
    this.getUnReadNotification();
  }

  public getUnReadNotification(): void {
    this.loading = true;
    this.unReadNotification = [];
    this.notificationService.getNotification(this.projectId, this.userId, 'unread').subscribe((response) => {
      this.loading = false;
      this.unReadNotification = response.result;
      this.unReadNotification.sort((a, b) => b.notificationId - a.notificationId);
      this.unReadNotification = this.parseResponse(this.unReadNotification);
    }, () => {
      this.loading = false;
      this.unReadNotification = [];
    });
  }

  public getAllNotification(): void {
    this.loading = true;
    this.allNotification = [];
    this.notificationService.getNotification(this.projectId, this.userId, 'all').subscribe((response) => {
      this.loading = false;
      this.allNotification = response.result;
      this.allNotificationGrp = _.groupBy(response.result, x => new Date(x.creationDate).toDateString());
    }, () => {
      this.loading = false;
      this.allNotification = [];
    });
  }

  public getNoticicationDate(date: Date): string {
    const todayDate = new Date().getDate();
    if (todayDate - 1 == new Date(date).getDate())
      return 'Yesterday';

    return this.datePipe.transform(new Date(date), 'dd-MM-yy');
  }

  public hideAllNotification(notificationId: number): void {
    this.notificationService.hideNotification(notificationId).subscribe((response) => {
      const index = this.allNotification.findIndex(x => x.notificationId === notificationId);
      this.allNotification.splice(index, 1);
      this.snakbar.open(response?.message);
    }, (error) => {
      this.snakbar.open(error);
    });
  }

  public hideUnreadNotification(notificationId: number): void {
    this.notificationService.hideNotification(notificationId).subscribe((response) => {
      const index = this.unReadNotification.findIndex(x => x.notificationId === notificationId);
      this.unReadNotification.splice(index, 1);
      this.snakbar.open(response?.message);
    }, (error) => {
      this.snakbar.open(error);
    });
  }

  private parseResponse(n: Array<notification>): Array<notification> {
    n.forEach(t => {
      t.notificationMsg = `${t.notificationMsg} ${this.datePipe.transform(t?.creationDate, 'dd-MM-yy, hh:mm a')}`;
      t.toolTipText = `${t.notificationMsg?.replace(/"/g, '')}`;
      t.isTruncated = t.toolTipText.length >= 54;
    });
    return n;
  }
}
