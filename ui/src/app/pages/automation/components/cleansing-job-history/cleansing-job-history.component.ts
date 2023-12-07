import { Component, OnInit } from '@angular/core';
import { CleansingJobHistory } from '../../models/cleansing-job-histroy';
import { AutomationService } from '../../services/automation.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { takeUntil } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-cleansing-job-history',
  templateUrl: './cleansing-job-history.component.html',
  styleUrls: ['./cleansing-job-history.component.scss']
})
export class CleansingJobHistoryComponent implements OnInit {

  private unsubscribe: Subject<void> = new Subject();
  private certificate$: Observable<Certificate>;
  public cleansingJobHistorys: Array<CleansingJobHistory> = [];
  public isDataSetNameByDescending: boolean;
  public isJobStatusByDescending: boolean;
  public projectName: string;

  public loading: boolean;
  public folderId: number;
  public projectId: number;
  public userId: number;
  public jobName: string;

  searchTerm: any = { fileName: '' };

  public page = 1;
  public startIndex: number = 0;
  public pageSize: number = 15;
  public endIndex: number = this.pageSize;

  private currnetPage: number;

  constructor(
    private quantumFacade: Quantumfacade,
    private automationService: AutomationService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private snakbar: SnackbarService) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });

    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
    this.folderId = +this.activatedRoute.snapshot.paramMap.get('jobId');
    this.jobName = this.activatedRoute.snapshot.paramMap.get('jobName');
    this.projectName = localStorage.getItem('projectname');
  }

  ngOnInit(): void {
    this.getCleansingHistory();

  }

  public getCleansingHistory(): void {

    this.loading = true;
    const params = {
      folderId: this.folderId,
      projectId: this.projectId,
      userId: this.userId
    };

    this.automationService.getCleansingHistory(params)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response: any) => {
        this.loading = false;
        this.cleansingJobHistorys = response?.result;

        this.cleansingJobHistorys.map((t, index) => {
          t.index = index + 1;
        })

      }, () => {
        this.loading = false;
      })
  }

  public onPageChange(currentPage: number): void {
    this.currnetPage = currentPage;
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public getCleanseDownloadFile(history: CleansingJobHistory): void {

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

  public delete(history: CleansingJobHistory): void {

    this.loading = true;
    this.automationService.deleteJobFile(this.projectId, this.userId, history.runJobId).subscribe((res) => {
      this.loading = false;
      if (res.code === 200) {
        const index = this.cleansingJobHistorys.findIndex(x => x.runJobId == history.runJobId);
        this.cleansingJobHistorys.splice(index, 1);

        const noOfPages = this.cleansingJobHistorys.length / this.pageSize;

        if (Math.ceil(noOfPages) < this.currnetPage) {
          this.currnetPage = this.currnetPage - 1;
          this.startIndex = (this.currnetPage - 1) * this.pageSize;
          this.endIndex = this.startIndex + this.pageSize;
        }

        this.snakbar.open('Cleansing job deleted successfully.');

        if(!this.cleansingJobHistorys.length){
          this.back();
        }
      } else {
        this.snakbar.open(res.message);
      }
     
    }, () => {
      this.loading = false;
    });

  }

  public runJob(history: CleansingJobHistory): void {
    this.loading = true;
    const params = {
      projectId: this.projectId,
      fileId: history.fileId
    };

    this.automationService.runJob(params).subscribe((res) => {
      if (res.code === 200) {
        this.loading = false;
        this.snakbar.open('Cleansing job re-run initiated successfully.');
        this.refresh();
      } else {
        this.snakbar.open(res?.message);
      }
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
  }

  public refresh(): void {
    this.getCleansingHistory();
  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/automation/cleansing`]);
  }

  public sortDataSetName(): void {
    this.isDataSetNameByDescending = !this.isDataSetNameByDescending;

    if (this.isDataSetNameByDescending) {
      this.cleansingJobHistorys = this.cleansingJobHistorys.sort((a, b) => {
        var fileName_order = a.fileName.localeCompare(b.fileName);
        return fileName_order;
      });
    } else {
      this.cleansingJobHistorys = this.cleansingJobHistorys.sort((a, b) => {
        var fileName_order = b.fileName.localeCompare(a.fileName);
        return fileName_order;
      });
    }
  }

  public sortByJobStatus(): void {
    this.isJobStatusByDescending = !this.isJobStatusByDescending;

    if (this.isJobStatusByDescending) {
      this.cleansingJobHistorys = this.cleansingJobHistorys.sort((a, b) => {
        var status_order = a.status.localeCompare(b.status);
        return status_order;
      });
    } else {
      this.cleansingJobHistorys = this.cleansingJobHistorys.sort((a, b) => {
        var status_order = b.status.localeCompare(a.status);
        return status_order;
      });
    }
  }

  public viewLogs(history: CleansingJobHistory): void {
    localStorage.setItem('batchJobLog', history?.batchJobLog);
    this.router.navigate([]).then(() => { window.open(`/projects/${this.projectId}/automation/logs`, '_blank'); });
  }
}
