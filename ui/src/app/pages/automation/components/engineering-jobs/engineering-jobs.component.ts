import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { AutomationService } from '../../services/automation.service';
import { EngineeringHistoryComponent } from '../engineering-history/engineering-history.component';
import { SelectFilesComponent } from '../select-files/select-files.component';

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
  engineeringJobs$: Observable<any>;
  loading: boolean;
  searchTerm: any = { engFlowName: '' };

  constructor(
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private automationService: AutomationService,
    private activatedRoute: ActivatedRoute) {

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
  }

  ngOnInit(): void {
    this.loading = true;
    this.engineeringJobs$ = this.automationService.getEngineeringJobs(this.userId, this.projectId).pipe(map((res) => {
      this.loading = false;
      return res.result;
    }, (error) => {
      this.loading = false;
    }));
  }

  openModal(job: any): void {
    const modalRef = this.modalService.open(EngineeringHistoryComponent, { size: 'lg', scrollable: true });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.flowId = job.engFlowId;

    modalRef.result.then((result) => {

    }, (reason) => {
      console.log(reason);
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

}
