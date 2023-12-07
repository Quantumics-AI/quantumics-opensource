import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { UdfService } from '../../services/udf.service';
import { CreateUpdateUdfComponent } from '../create-update-udf/create-update-udf.component';
import { ConfirmationDialogComponent } from 'src/app/core/components/confirmation-dialog/confirmation-dialog.component';
import { FilterPipe } from 'ngx-filter-pipe';
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';

@Component({
  selector: 'app-udf-list',
  templateUrl: './udf-list.component.html',
  styleUrls: ['./udf-list.component.scss']
})
export class UdfListComponent implements OnInit {

  private unsubscribe: Subject<void> = new Subject<void>();
  private userId: number;
  
  public projectId: number;
  public udfList = [];
  public originalUdfList = [];
  public loading: boolean;
  public searchTerm: any = { udfName: '' };

  public page = 1;
  public startIndex: number = 0;
  public pageSize: number = 10;
  public endIndex: number = this.pageSize;

  private currnetPage: number;

  public isDescending: boolean;

  public searchDiv: boolean = false;
  public searchString: string;
  public hasFolders: boolean;

  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private modalService: NgbModal,
    private udfService: UdfService,
    private snackbar: SnackbarService,
    private folderService: FolderService,
    private filterPipe: FilterPipe) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
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
        this.getUdfList();
      } else {
        this.hasFolders = false;
      }
    }, () => {
      this.hasFolders = false;
    });
  }

  openCreateUDFModal(): void {
    const modalRef: NgbModalRef = this.modalService.open(CreateUpdateUdfComponent, { size: 'lg' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.isUpdate = false;

    modalRef.result.then((result) => {
      if (result) {
        this.getUdfList();
      }
    }, (reason) => {

    });
  }

  private getUdfList(): void {

    this.udfList = [];
    this.loading = true;

    this.udfService.getUdfList(this.projectId, this.userId)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response: any) => {
        this.loading = false;
        this.udfList = response.result;
        this.udfList.sort((a, b) => b.udfId - a.udfId);
        this.originalUdfList = [...this.udfList];
      }, (error) => {
        this.loading = false;
        this.udfList = [];
      })
  }

  public edit(udf: any): void {
    const modalRef: NgbModalRef = this.modalService.open(CreateUpdateUdfComponent, { size: 'lg' });
    modalRef.componentInstance.udf = udf;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.isUpdate = true;
    modalRef.result.then((res) => {
    }, (reason) => {
      console.log(reason);
    });
  }

  public delete(udf: any): void {

    const modalRef = this.modalService.open(ConfirmationDialogComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.title = 'Confirmation';
    modalRef.componentInstance.message = 'You are going to delete the user defined function. All the flows having this udf will be affected and functionality will be lost.';
    modalRef.componentInstance.btnOkText = 'Delete';
    modalRef.componentInstance.btnCancelText = 'Cancel';

    modalRef.result.then((result) => {
      if (result) {
        this.loading = true;
        this.udfService.deleteUdf(this.projectId, this.userId, udf.udfId)
          .pipe(takeUntil(this.unsubscribe))
          .subscribe((result: any) => {
            this.loading = false;
            if (result.code === 200) {
              const index = this.udfList.findIndex(x => x.udfId === udf.udfId);
              this.udfList.splice(index, 1); // remove the element.
              const noOfPages = this.udfList.length / this.pageSize;

              if (Math.ceil(noOfPages) < this.currnetPage) {
                this.currnetPage = this.currnetPage - 1;
                this.startIndex = (this.currnetPage - 1) * this.pageSize;
                this.endIndex = this.startIndex + this.pageSize;
              }
            }

            this.snackbar.open(`${result.message}`);
          }, (error) => {
            this.loading = false;
            this.snackbar.open(`${error}`);
          });
      }
    }, (result) => {

    });
  }

  public onPageChange(currentPage: number): void {
    this.currnetPage = currentPage;
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public sortUDF(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.udfList = this.udfList.sort((a, b) => {
        return a.udfName.localeCompare(b.udfName);
      });
    } else {
      this.udfList = this.udfList.sort((a, b) => {
        return b.udfName.localeCompare(a.udfName);
      });
    }
  }

  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
      this.udfList = this.originalUdfList;
    } else {
      this.searchDiv = true;
      this.udfList = this.filterPipe.transform(this.originalUdfList, { udfName: this.searchString });
      this.startIndex = 0;
    }
  }

  clearSearhInput() {
    this.searchTerm.udfName = '';
    this.searchDiv = false;
    this.udfList = this.originalUdfList;
    this.page = 1;
    this.startIndex = 0;
    this.endIndex = this.pageSize;
  }
}
