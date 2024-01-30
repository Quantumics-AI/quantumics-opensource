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
import { SourceDataService } from 'src/app/pages/ingest/services/source-data.service';
import { FilterPipe } from 'ngx-filter-pipe';

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
  // cleansingJobs$: Observable<any>;
  public cleansingJobs: any;
  public originalCleansingJobs: Array<any>;
  projectName: string;
  searchTerm: any = { folderDisplayName: '' };
  userId: number;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  total_folders: number;
  folders: any;

  public searchDiv: boolean = false;
  public searchString: string;
  public startIndex: number = 0;
  public pageSize: number = 10;
  public endIndex: number = this.pageSize;
  public page = 1;

  public isDescending: boolean;

  public queryFolders: boolean;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private quantumFacade: Quantumfacade,
    private modalService: NgbModal,
    private cleansingDataService: CleansingDataService,
    private sourceDataService: SourceDataService,
    private filterPipe: FilterPipe
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
    this.foldersList();

  }

  foldersList() {
    this.loading = true;
    this.sourceDataService.getFolders(+this.projectId, this.userId).subscribe((response) => {
      this.loading = false;
      this.folders = response;
      console.log("----", response);
      if (this.folders.length > 0) {
        this.cleanseList();
      }

    }, () => {
      this.loading = false;
    });
  }

  cleanseList() {
    this.loading = true;
    this.cleansingDataService.getCleansingJobs(+this.projectId, this.userId).subscribe((res) => {
      this.loading = false;
      if (res.code !== 200) {
        this.cleansingJobs = [];
      } else {
        this.cleansingJobs = res.result;
        if (this.cleansingJobs.length > 0) {
          this.cleansingJobs.sort((val1, val2) => {
            return (
              (new Date(val2.modifiedDate) as any) -
              (new Date(val1.modifiedDate) as any)
            );
          });
          this.originalCleansingJobs = [...this.cleansingJobs];
        }
        this.hasData = res?.length > 0;
        this.loading = false;
        this.total_folders = res.length;
      }

    }, (error) => {
      this.loading = false;
      this.cleansingJobs = [];
    });
    // this.cleansingJobs$ = this.cleansingDataService.getCleansingJobs(+this.projectId, this.userId).pipe(
    //   catchError(() => {
    //     this.loading = false;
    //     return of({
    //       result: []
    //     });
    //   }),
    //   map(res => res.result),
    //   tap((res) => {
    //     res.sort((val1, val2) => {
    //       return (
    //         (new Date(val2.modifiedDate) as any) -
    //         (new Date(val1.modifiedDate) as any)
    //       )
    //     });

    //     this.hasData = res?.length > 0;
    //     this.loading = false;
    //     this.total_folders = res.length;
    //   }));
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

  //new table
  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
      this.cleansingJobs = this.originalCleansingJobs;
    } else {
      this.searchDiv = true;
      this.cleansingJobs = this.filterPipe.transform(this.originalCleansingJobs, { folderDisplayName: this.searchString });
      this.startIndex = 0;
    }
  }

  clearSearhInput() {
    this.searchTerm = { folderDisplayName: '' };
    this.searchDiv = false;
    this.cleansingJobs = this.originalCleansingJobs;
    this.page = 1;
    this.startIndex = 0;
    this.endIndex = this.pageSize;
  }

  public onPageChange(currentPage: number): void {
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public sortFolderName(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.cleansingJobs = this.cleansingJobs.sort((a, b) => {
        var folder_name_order = a.folderDisplayName.localeCompare(b.folderDisplayName);
        return folder_name_order;
      });
    } else {
      this.cleansingJobs = this.cleansingJobs.sort((a, b) => {
        var folder_name_order = b.folderDisplayName.localeCompare(a.folderDisplayName);
        return folder_name_order;
      });
    }

  }

  public openD(folderId: number, fileName: string, folderName: string): void {
    this.router.navigate([`projects/${this.projectId}/cleansing/jobs`], {
      queryParams: {
        folderId: folderId,
        filename: fileName,
        foldername: folderName
      }
    });
  }

  public onItemChange(item: string): void {

  }
}
