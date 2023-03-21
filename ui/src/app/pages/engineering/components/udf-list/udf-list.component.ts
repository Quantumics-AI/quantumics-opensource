import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { UdfService } from '../../services/udf.service';
import { CreateUpdateUdfComponent } from '../create-update-udf/create-update-udf.component';

@Component({
  selector: 'app-udf-list',
  templateUrl: './udf-list.component.html',
  styleUrls: ['./udf-list.component.scss']
})
export class UdfListComponent implements OnInit {

  private unsubscribe: Subject<void> = new Subject<void>();
  private projectId: number;
  private userId: number;

  public udfList = [];
  public loading: boolean;
  public searchTerm: any = { udfName: '' };

  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private modalService: NgbModal,
    private udfService: UdfService,
    private snackbar: SnackbarService) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
  }

  ngOnInit(): void {
    this.getUdfList();
  }

  openCreateUDFModal(): void {
    const modalRef: NgbModalRef = this.modalService.open(CreateUpdateUdfComponent, { size: 'lg' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.isUpdate = false;

    modalRef.result.then((res) => {
    }, (reason) => {
      console.log(reason);
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
      }, (error) => {
        this.loading = false;
        this.snackbar.open(`${error}`);
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
    if (!confirm(`Are you sure you want to delete ${udf.udfName}`)) {
      return;
    }

    this.udfService.deleteUdf(this.projectId, this.userId, udf.udfId)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((result: any) => {
        if (result.code === 200) {
          const index = this.udfList.findIndex(x => x.udfId === udf.udfId);
          this.udfList.splice(index, 1); // remove the element.
        }

        this.snackbar.open(`${result.message}`);
      }, (error) => {
        this.snackbar.open(`${error}`);
      });
  }
}
