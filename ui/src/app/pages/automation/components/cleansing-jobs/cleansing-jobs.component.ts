import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { AutomationService } from '../../services/automation.service';
import { CleansingHistoryComponent } from '../cleansing-history/cleansing-history.component';
import { CleansingSelectFilesComponent } from '../cleansing-select-files/cleansing-select-files.component';

@Component({
  selector: 'app-cleansing-jobs',
  templateUrl: './cleansing-jobs.component.html',
  styleUrls: ['./cleansing-jobs.component.scss']
})
export class CleansingJobsComponent implements OnInit {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  loading: boolean;
  userId: number;
  projectId: number;
  folderId: number;
  searchTerm: any = { folderDisplayName: '' };

  cleansingJobs$: Observable<any>;
  constructor(
    private modalService: NgbModal,
    private automationService: AutomationService,
    private quantumFacade: Quantumfacade,
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
    this.cleansingJobs$ = this.automationService.getCleansingJobs(this.projectId, this.userId).pipe(map((res) => {
      this.loading = false;
      return res.result;
    }, (error) => {
      this.loading = false;
    }));
  }

  openModal(job: any): void {
    this.folderId = job.folderId;
    const modalRef = this.modalService.open(CleansingHistoryComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.folderId = job.folderId;

    modalRef.result.then((result) => {
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }

  selectFiles(job: any): void {
    this.folderId = job.folderId;

    const modalRef = this.modalService.open(CleansingSelectFilesComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.folderId = job.folderId;

    modalRef.result.then((result) => {

    }, (reason) => {
      console.log(reason);
    });
  }
}
