import { Component, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { ConfirmationDialogComponent } from '../../../../core/components/confirmation-dialog/confirmation-dialog.component';
import { EngineeringService } from '../../services/engineering.service';
import { SharedService } from '../../services/shared.service';
import { AddComponent } from '../add/add.component';
import { EditFlowComponent } from '../edit-flow/edit-flow.component';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent {
  @Input() flows: Array<any>;
  public userId: number;
  public projectId: number;
  public projectName: string;
  public loading: boolean;
  public searchTerm: any = { engFlowName: '' };

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private sharedService: SharedService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private engrngService: EngineeringService,
    private modalService: NgbModal,
    private snackbar: SnackbarService) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
  }

  edit(flow: any): void {
    const modalRef: NgbModalRef = this.modalService.open(EditFlowComponent, { size: 'lg' });
    modalRef.componentInstance.flow = flow;
    modalRef.result.then((res) => {
      const flow = this.flows.find(x => x.engFlowId === res.engFlowId);
      flow.engFlowName = res.engFlowName;
      flow.engFlowDesc = res.engFlowDesc;
    }, (reason) => {
      console.log(reason);
    });
  }

  redirectToCanvas(flow: any): void {
    this.router.navigate([`${flow.engFlowId}/edit/${flow.engFlowDisplayName}`], { relativeTo: this.activatedRoute });
  }

  view(flowId: number, engFlowName: string): void {
    this.router.navigate([`${flowId}/view/${engFlowName}`], { relativeTo: this.activatedRoute });
  }

  openModal(): void {
    const modalRef: NgbModalRef = this.modalService.open(AddComponent, { size: 'lg' });
    modalRef.result.then((res) => {
      this.sharedService.createFlow(res);
    }, (reason) => {
      console.log(reason);
    });
  }

  delete(flowId: number): void {

    const modalRef = this.modalService.open(ConfirmationDialogComponent, { size: 'sm' });
    modalRef.componentInstance.title = 'Confirmation';
    modalRef.componentInstance.message = 'Are you sure you want to delete the Flow?';
    modalRef.componentInstance.btnOkText = 'Delete';
    modalRef.componentInstance.btnCancelText = 'Cancel';

    modalRef.result.then((isDelete) => {
      if (isDelete) {
        this.engrngService.deleteFlow(this.projectId, this.userId, flowId).subscribe((res) => {
          this.snackbar.open(res.message);
          const index = this.flows.findIndex(x => x.engFlowId === flowId);
          this.flows.splice(index, 1);
        });
      }
    });
  }
}
