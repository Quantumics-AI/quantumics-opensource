import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { AutomationService } from '../../services/automation.service';

@Component({
  selector: 'app-engineering-history',
  templateUrl: './engineering-history.component.html',
  styleUrls: ['./engineering-history.component.scss']
})
export class EngineeringHistoryComponent implements OnInit {
  private userId: number;
  private flowId: number;
  private certificate$: Observable<Certificate>;
  public isDescendingByJobRun: boolean;
  public isDescendingByJobStatus: boolean;

  public projectId: number;
  public jobHistory: Array<any> = [];
  public loading: boolean;
  private unsubscribe: Subject<void> = new Subject<void>();
  public dataFlowName: string;
  public searchTerm: any = { engFlowName: '' };

  public curentPage: number;
  public startIndex: number = 0;
  public pageSize: number = 15;
  public endIndex: number = this.pageSize;
  public page = 1;

  constructor(
    private router: Router,
    private automationService: AutomationService,
    private snakbar: SnackbarService,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
  ) {
    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (Object.keys(params).length) {
        this.dataFlowName = params.flowname;
        this.flowId = params.flowid;
        this.getEngineeringHistory();
      }
    });
  }

  public getEngineeringHistory(): void {
    this.loading = true;
    const params = {
      engFlowId: this.flowId,
      userId: this.userId,
      projectId: this.projectId
    };

    this.automationService.getEngineeringHistory(params).subscribe((response: any) => {
      this.loading = false;
      this.jobHistory = response.result;

      this.jobHistory.map((t, index) => {
        t.index = index + 1;
      });

    }, (error) => {
      this.loading = false;
    });
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
    // this.modal.dismiss();
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

  public clearSearhInput(): void {
    this.searchTerm.engFlowName = '';
  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/automation/engineering`]);
  }

  public onPageChange(currentPage: number): void {
    this.curentPage = currentPage;
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public sortByProperty<T>(array: T[], property: keyof T, isDescending: boolean): void {
    array.sort((a, b) => {
      const propA = a[property] as unknown as string;
      const propB = b[property] as unknown as string;

      if (typeof propA === 'string' && typeof propB === 'string') {
        return isDescending ? propB.localeCompare(propA) : propA.localeCompare(propB);
      }

      return 0;  // Return 0 if the properties are not strings
    });
  }
}
