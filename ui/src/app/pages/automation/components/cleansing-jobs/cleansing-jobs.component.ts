import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { AutomationService } from '../../services/automation.service';
import { CleansingJobs } from '../../models/cleansing-jobs';
import { CleansJobFolder } from '../../models/cleans-job-folder';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { CleansedRunedJob } from '../../models/cleansed-runed-job';
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';
import { PAGE_SIZE } from 'src/app/core/const/pageSize';

@Component({
  selector: 'app-cleansing-jobs',
  templateUrl: './cleansing-jobs.component.html',
  styleUrls: ['./cleansing-jobs.component.scss']
})
export class CleansingJobsComponent implements OnInit {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private userId: number;

  public projectId: number;
  public loading: boolean;
  public reset: boolean;
  public searchTerm: any = { folderDisplayName: '' };
  public page = 1;
  public startIndex: number = 0;
  public pageSize: number = PAGE_SIZE;
  public endIndex: number = this.pageSize;
  public projectName: string;

  public isFolderNameByDescending: boolean;
  public isJobStatusByDescending: boolean;
  public hasFolders: boolean;

  cleansedRunedJob: Array<CleansedRunedJob> = [];
  constructor(
    private automationService: AutomationService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private snakbar: SnackbarService,
    private folderService: FolderService,
    private router: Router) {

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id
      });

    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
    this.projectName = localStorage.getItem('projectname');
  }

  ngOnInit(): void {
    this.foldersList();
  }

  foldersList() {
    this.loading = true;
    this.folderService.getFolders(this.projectId, this.userId).subscribe((response: any) => {
      this.hasFolders = response.result?.length;
      this.loading = false;
      if (this.hasFolders) {
        this.getCleansingRunJobs();
      }
    }, () => {
      this.hasFolders = false;
      this.loading = false;
    });
  }

  public getCleansingRunJobs(): void {
    this.loading = true;
    this.automationService.getCleansingRunJobs(this.projectId, this.userId).subscribe((response) => {
      this.cleansedRunedJob = response.result;
      this.loading = false;
      return response.result;
    }, (error) => {
      this.loading = false;
    });
  }

  public onPageChange(currentPage: number): void {
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public runJob(request: CleansJobFolder): void {
    this.loading = true;
    const params = {
      projectId: this.projectId,
      fileId: request.fileId
    };

    this.automationService.runJob(params).subscribe((res) => {
      if (res.code === 200) {
        this.reset = !this.reset;
        this.loading = false;
        this.snakbar.open(res.message);
        this.getCleansingRunJobs();
      } else {
        this.snakbar.open(res?.message);
      }
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });

  }

  public redirectToCleanseJobHistroy(job: CleansingJobs): void {
    this.router.navigate([`projects/${this.projectId}/automation/cleansing/history/${job.folderId}/${job.folderDisplayName}`]);
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
