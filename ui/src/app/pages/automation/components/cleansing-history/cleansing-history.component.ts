import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { AutomationService } from '../../services/automation.service';
import { map } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cleansing-history',
  templateUrl: './cleansing-history.component.html',
  styleUrls: ['./cleansing-history.component.scss']
})
export class CleansingHistoryComponent implements OnInit {
  projectId: number;
  userId: number;
  folderId: number;
  history$: Observable<any>;
  loading: boolean;

  constructor(
    private router: Router,
    public modal: NgbActiveModal,
    private automationService: AutomationService,
    private snakbar: SnackbarService
  ) { }

  ngOnInit(): void {
    this.getCleansingHistory();
  }

  private getCleansingHistory(): void {
    this.loading = true;
    const params = {
      folderId: this.folderId,
      projectId: this.projectId,
      userId: this.userId
    };

    this.history$ = this.automationService.getCleansingHistory(params).pipe(
      map((res: any) => {
        this.loading = false;
        if (res.code === 200) {
          return res.result;
        } else {
          return [];
        }
      }, (error) => {
        this.loading = false;
      }));
  }

  getCleanseDownloadFile(history: any): void {
    if (history.status.toLowerCase() !== 'succeeded') {
      return;
    }

    this.loading = true;
    const params = {
      projectId: this.projectId,
      type: 'processed',
      folderId: history.folderId,
      fileId: history.fileId,
      jobId: history.runJobId
    };

    this.automationService.getCleanseDownloadFile(this.userId, params).subscribe((res) => {
      this.loading = false;
      const blob = new Blob([res], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.download = history.fileName;
      anchor.href = url;
      anchor.click();
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
  }

  delete(history: any): void {
    this.loading = true;
    this.automationService.deleteJobFile(this.projectId, this.userId, history.runJobId).subscribe((res) => {
      if (res.code === 200) {
        this.getCleansingHistory();
        this.snakbar.open('Cleansing job deleted successfully.');
      } else {
        this.snakbar.open(res.message);
      }
      this.loading = false;
    });
  }

  runJob(history: any): void {
    const params = {
      projectId: this.projectId,
      fileId: history.fileId
    };

    this.automationService.runJob(params).subscribe((res) => {
      if (res.code === 200) {
        this.snakbar.open('Cleansing job re-run initiated successfully.');
        this.refresh();
      } else {
        this.snakbar.open(res?.message);
      }
    }, (error) => {
      this.snakbar.open(error);
    });
  }

  public viewLogs(history: any): void {
    localStorage.setItem('batchJobLog', history?.batchJobLog);
    this.router.navigate([]).then(() => { window.open(`/projects/${this.projectId}/automation/logs`, '_blank'); });
  }

  public refresh(): void {
    this.getCleansingHistory();
  }

  public isEllipsisActive(e: HTMLElement): boolean {
    return e ? (e.offsetWidth < e.scrollWidth) : false;
  }
}
