import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ListRawDataComponent } from '../list-raw-data/list-raw-data.component';
import { ActivatedRoute, Router } from '@angular/router';
import { CleansingDataService } from '../../services/cleansing-data.service';
import { catchError, map, takeUntil, tap } from 'rxjs/operators';
import { Observable, of, Subject } from 'rxjs';
import { ListCleansedFilesComponent } from '../list-cleansed-files/list-cleansed-files.component';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';

@Component({
  selector: 'app-add-cleansing',
  templateUrl: './add-cleansing.component.html',
  styleUrls: ['./add-cleansing.component.scss']
})
export class AddCleansingComponent implements OnInit {
  private unsubscribe: Subject<void> = new Subject();

  projectId: string;
  selectionChange: boolean;
  loading = false;
  hasData = false;
  cleansingJobs$: Observable<any>;
  projectName: string;
  searchTerm: any = { folderDisplayName: '' };
  userId: number;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  total_folders: number;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private quantumFacade: Quantumfacade,
    private modalService: NgbModal,
    private cleansingDataService: CleansingDataService
  ) {

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +this.certificateData.user_id;
      });
  }

  ngOnInit(): void {
    this.loading = true;
    this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.projectName = localStorage.getItem('projectname');
    this.cleansingJobs$ = this.cleansingDataService.getCleansingJobs(+this.projectId, this.userId).pipe(
      catchError(() => {
        this.loading = false;
        return of({
          result: []
        });
      }),
      map(res => res.result),
      tap((res) => {
        res.sort((val1, val2) => {
          return (
            (new Date(val2.modifiedDate) as any) -
            (new Date(val1.modifiedDate) as any)
          )
        });
        
        this.hasData = res?.length > 0;
        this.loading = false;
        this.total_folders = res.length;
      }));
  }

  open() {
    this.selectionChange = !this.selectionChange;
  }

  openRuleAddedFiles(folderId: number, fileName: string, folderName: string) {
    const modalRef = this.modalService.open(ListCleansedFilesComponent, {
      size: 'lg',
      windowClass: 'modal-size'
    });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.folderId = folderId;
    modalRef.componentInstance.fileName = fileName;
    modalRef.componentInstance.folderName = folderName;
    modalRef.result.then(
      result => {
        console.log(result);
      },
      reason => {
        console.log(reason);
      }
    );
  }
}
