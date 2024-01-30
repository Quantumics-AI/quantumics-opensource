import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { AutomationService } from '../../services/automation.service';
import { EngineeringHistoryComponent } from '../engineering-history/engineering-history.component';
import { SelectFilesComponent } from '../select-files/select-files.component';
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';
import { PAGE_SIZE } from 'src/app/core/const/pageSize';

@Component({
  selector: 'app-engineering-jobs',
  templateUrl: './engineering-jobs.component.html',
  styleUrls: ['./engineering-jobs.component.scss']
})
export class EngineeringJobsComponent implements OnInit {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  userId: number;
  projectId: number;
  public engineeringJobs: Array<any> = [];
  loading: boolean;
  searchTerm: any = { engFlowName: '' };

  public hasFolders: boolean;
  public searchString: string;
  public isDescendingFlowName: boolean;
  public isDescendingFlowStatu: boolean;
  public startIndex: number = 0;
  public pageSize: number = PAGE_SIZE;
  public endIndex: number = this.pageSize;
  public page = 1;

  public flowid: number;
  public execute: boolean = false;

  private curentPage: number = 1;

  constructor(
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private automationService: AutomationService,
    private activatedRoute: ActivatedRoute,
    private folderService: FolderService,
    private router: Router,) {

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
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
        this.getEngineeringJobs();
      } else {

      }
    }, () => {
      this.hasFolders = false;
      this.loading = false;
    });
  }

  public getEngineeringJobs(): void {
    this.loading = true;
    this.automationService.getEngineeringJobs(this.userId, this.projectId).subscribe((res) => {
      this.loading = false;
      this.engineeringJobs = res.result;

      this.engineeringJobs.map(t => {
        if (!t.jobStatus) {
          t.jobStatus = '';
        }
      })

      this.flowid = +localStorage.getItem('flowId');
      if (this.flowid) {
        this.curentPage = +localStorage.getItem('currentPage');
        this.page = this.curentPage;
        this.onPageChange(this.curentPage);
        this.execute = true;
        setTimeout(() => {
          this.execute = false;
          localStorage.removeItem('flowId');
          localStorage.removeItem('currentPage');
        }, 5000);
      }
    }, (error) => {
      this.loading = false;
    });
  }

  public openHistory(job: any): void {
    this.router.navigate([`projects/${this.projectId}/automation/engineering/history`], {
      queryParams: { flowname: job.engFlowDisplayName, flowid: job.engFlowId }
    });
  }

  selectFiles(job: any): void {
    const modalRef = this.modalService.open(SelectFilesComponent, { size: 'xl', scrollable: true });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.flowId = job.engFlowId;

    modalRef.result.then((result) => {

    }, (reason) => {
      console.log(reason);
    });
  }

  public clearSearhInput(): void {
    this.searchTerm.engFlowName = '';
  }

  public onPageChange(currentPage: number): void {
    this.curentPage = currentPage;
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public selectJob(job: any): void {
    localStorage.setItem('currentPage', `${this.curentPage}`);
    this.router.navigate([`projects/${this.projectId}/automation/engineering/dataset`], {
      queryParams: { flowname: job.engFlowDisplayName, flowid: job.engFlowId }
    });
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
