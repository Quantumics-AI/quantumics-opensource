import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { SourceDataService } from '../../services/source-data.service';
import { DeleteFolderDatasetComponent } from '../delete-folder-dataset/delete-folder-dataset.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FilterPipe } from 'ngx-filter-pipe';
import { PAGE_SIZE } from 'src/app/core/const/pageSize';

@Component({
  selector: 'app-dataset-list',
  templateUrl: './dataset-list.component.html',
  styleUrls: ['./dataset-list.component.scss']
})
export class DatasetListComponent implements OnInit, OnDestroy {

  public projectId: number;
  private folderId: number;
  private userId: number;
  public folderName: string;

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private certificateData: Certificate;

  public searchDiv: boolean = false;
  public searchString: string;
  public searchTerm: any = { fileName: '' };
  public filesList: Array<any> = [];
  public originalFilesList: Array<any> = [];
  public loading: boolean;
  public isDescending: boolean;

  public startIndex: number = 0;
  public pageSize: number = PAGE_SIZE;
  public endIndex: number = this.pageSize;

  private currentPage: number;

  public page = 1;

  constructor(
    private router: Router,
    private snakbar: SnackbarService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private sourceDataService: SourceDataService,
    private modalService: NgbModal,
    private filterPipe: FilterPipe
  ) {
    this.projectId = +this.activatedRoute.snapshot.parent.paramMap.get('projectId');
    this.folderId = +this.activatedRoute.snapshot.paramMap.get('folderId');
    this.folderName = localStorage.getItem('selectedFolderName') as string;

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.certificateData = certificate;
          this.userId = +this.certificateData.user_id;
        }
      });
  }

  ngOnInit(): void {
    this.getPipeLineFiles();
  }

  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
      this.filesList = this.originalFilesList;
    } else {
      this.searchDiv = true;
      this.filesList = this.filterPipe.transform(this.originalFilesList, { fileName: this.searchString });
      this.startIndex = 0;
    }
  }

  private getPipeLineFiles(): void {
    this.loading = true;
    this.sourceDataService.getListData(+this.projectId, this.userId, this.folderId).subscribe((response: any) => {

      this.loading = false;
      if (response.code !== 200) {
        this.snakbar.open(response.message);
        this.filesList = [];
      }
      else {
        this.filesList = response.result;
        if (this.filesList.length > 1) {
          this.filesList.sort((a, b) => b.createdDate - a.createdDate);
        }
      }

      this.originalFilesList = [...this.filesList];
    }, () => {
      this.loading = false;
    });
  }

  public redirectToPipeLine(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/pipelines/`]);
  }

  public redirectToFileView(file: any) {
    this.router.navigate([`projects/${this.projectId}/ingest/pipelines/view`], {
      queryParams: {
        folderId: file.folderId,
        folderName: this.folderName,
        file: file.fileName,
        projectId: this.projectId,
        fId: file.fileId
      }
    });
  }

  public sortPipeLineFiles(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.filesList = this.filesList.sort((a, b) => {
        var fileName_order = a.fileName.localeCompare(b.fileName);
        return fileName_order;
      });
    } else {
      this.filesList = this.filesList.sort((a, b) => {
        var fileName_order = b.fileName.localeCompare(a.fileName);
        return fileName_order;
      });
    }

  }

  public formatFileSize(bytes): string {
    if (bytes < 1024) {
      return bytes + " bytes";
    } else if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(2) + " KB";
    } else if (bytes < 1024 * 1024 * 1024) {
      return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    } else if (bytes < 1024 * 1024 * 1024 * 1024) {
      return (bytes / (1024 * 1024 * 1024)).toFixed(2) + " GB";
    } else {
      return (bytes / (1024 * 1024 * 1024 * 1024)).toFixed(2) + " TB";
    }
  }

  delete(fileId: number, selectedFileName: string): void {
    const modalRef = this.modalService.open(DeleteFolderDatasetComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.fileId = fileId;
    modalRef.componentInstance.source = 'pipeline';

    modalRef.result.then((result) => {
      this.loading = true;
      // this.snakbar.open('Your delete request is in-progress.');
      this.sourceDataService.deleteFile(+this.projectId, +this.certificateData.user_id, this.folderId, fileId).subscribe((res) => {
        this.loading = false;
        if (res?.code == 200) {
          const idx = this.filesList.findIndex(x => x.fileId == fileId);
          this.filesList.splice(idx, 1);
          // this.getPipeLineFiles();
          this.snakbar.open(res?.message);

          const noOfPages = this.filesList.length / this.pageSize;
          
          if (Math.ceil(noOfPages) < this.currentPage) {
            this.currentPage = this.currentPage - 1;
            this.startIndex = (this.currentPage - 1) * this.pageSize;
            this.endIndex = this.startIndex + this.pageSize;
          }

        }
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });
    }, () => { });
  }

  clearSearhInput() {
    this.searchTerm = { fileName: '' };
    this.searchDiv = false;

    this.filesList = this.originalFilesList;
    this.page = 1;
    this.startIndex = 0;
    this.endIndex = this.pageSize;
  }

  public redirectToDF(file: any): void {
    this.router.navigate([`projects/${this.projectId}/delta/data-profile`], {
      queryParams: {
        folderId: file.folderId,
        fileId: file.fileId,
        sourcetype: 'pipeline'
      }
    });
  }

  public onPageChange(currentPage: number): void {
    this.currentPage = currentPage;
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
