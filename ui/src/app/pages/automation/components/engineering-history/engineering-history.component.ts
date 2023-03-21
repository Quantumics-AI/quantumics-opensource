import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { AutomationService } from '../../services/automation.service';

@Component({
  selector: 'app-engineering-history',
  templateUrl: './engineering-history.component.html',
  styleUrls: ['./engineering-history.component.scss']
})
export class EngineeringHistoryComponent implements OnInit {
  userId: number;
  projectId: number;
  flowId: number;
  history$: Observable<any>;
  loading: boolean;

  constructor(
    public modal: NgbActiveModal,
    private router: Router,
    private automationService: AutomationService,
    private snakbar: SnackbarService
  ) { }

  ngOnInit(): void {
    this.getEngineeringHistory();
  }

  private getEngineeringHistory(): void {
    this.loading = true;
    const params = {
      engFlowId: this.flowId,
      userId: this.userId,
      projectId: this.projectId
    };

    this.history$ = this.automationService.getEngineeringHistory(params).pipe(
      map((res: any) => {
        this.loading = false;
        if (res.code === 200) {
          return res.result;
        } else {
          return [];
        }
      }, (error) => {
        this.loading = false;
      })
    );
  }

  getEngDownloadFile(history: any): void {
    if (history.status.toLowerCase() !== 'succeeded') {
      return;
    }

    this.loading = true;

    const params = {
      projectId: this.projectId,
      engFlowId: history.engFlowId,
      type: 'engresult',
      jobId: history.flowJobId
    };

    this.automationService.getEngDownloadFile(this.userId, params).subscribe((res) => {
      this.loading = false;
      const blob = new Blob([res], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.download = `${history.engFlowName}.csv`;
      anchor.href = url;
      anchor.click();
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
  }

  view(flowId: number, flowName: string): void {
    this.router.navigate([`projects/${this.projectId}/engineering/engineer/${flowId}/view/${flowName}`]);
    this.modal.dismiss();
  }

  refresh(): void {
    this.getEngineeringHistory();
  }

  public reRun(history: any): void {
    const autorunReqPayload = JSON.parse(history.autorunReqPayload);
    this.automationService.runAutomation(autorunReqPayload).subscribe((res) => {
      this.snakbar.open('Process started');
      this.refresh();
    }, (error) => {
      this.snakbar.open(error);
    });
  }

  public viewLogs(history: any): void {
    localStorage.setItem('batchJobLog', history?.batchJobLog);
    this.router.navigate([]).then(() => { window.open(`/projects/${this.projectId}/automation/logs`, '_blank'); });
  }
}
