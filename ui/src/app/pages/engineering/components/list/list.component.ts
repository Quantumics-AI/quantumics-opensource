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
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';
import { FilterPipe } from 'ngx-filter-pipe';

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

  public hasFolders: boolean;
  public hasFlowList: boolean;
  public flowsData: any;
  public originalFlowsData: Array<any>;
  public searchDiv: boolean = false;
  public searchString: string;
  public isDescending: boolean;
  public startIndex: number = 0;
  public pageSize: number = 10;
  public endIndex: number = this.pageSize;
  public page = 1;

  constructor(
    private sharedService: SharedService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private engrngService: EngineeringService,
    private modalService: NgbModal,
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
        // this.getProjectStats();
        // this.loadData();
        this.getFlows();
      } else {

      }
    }, () => {
      this.hasFolders = false;
    });
  }

  public getFlows(): void {
    this.loading = true;
    this.engrngService.getEngineeringList(this.userId, this.projectId).subscribe((res) => {
      this.loading = false;
      this.flowsData = res?.result;
      this.flowsData.sort((a, b) => b.modifiedDate - a.modifiedDate);
      this.hasFlowList = res.result?.length;
      this.originalFlowsData = [...this.flowsData];
    }, () => {
      this.loading = false;
    });
  }

  edit(flow: any): void {
    const modalRef: NgbModalRef = this.modalService.open(EditFlowComponent, { size: 'md' });
    modalRef.componentInstance.flow = flow;
    modalRef.result.then((res) => {
      const flowObj = this.flowsData.find(x => x.engFlowId === res.engFlowId);
      flowObj.engFlowDisplayName = res.engFlowName;
      flowObj.engFlowDesc = res.engFlowDesc;
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
    const modalRef: NgbModalRef = this.modalService.open(AddComponent, { size: 'md' });
    modalRef.result.then((res) => {
      this.sharedService.createFlow(res);
    }, (reason) => {
      console.log(reason);
    });
  }

  delete(flowId: number): void {

    const modalRef = this.modalService.open(ConfirmationDialogComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.title = 'Confirmation';
    modalRef.componentInstance.message = 'You are going to delete the entire data flow. All the jobs related to the flow will be lost and can not be retrieved back.';
    modalRef.componentInstance.btnOkText = 'Delete';
    modalRef.componentInstance.btnCancelText = 'Cancel';

    modalRef.result.then((isDelete) => {
      if (isDelete) {
        this.loading = true;
        this.engrngService.deleteFlow(this.projectId, this.userId, flowId).subscribe((res) => {
          this.loading = false;
          this.snackbar.open(res.message);
          const index = this.flowsData.findIndex(x => x.engFlowId === flowId);
          this.flowsData.splice(index, 1);
          this.hasFlowList = this.flowsData.length;
        }, () => {
          this.loading = false;
        });
      }
    });
  }

  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
      this.flowsData = this.originalFlowsData;
    } else {
      this.searchDiv = true;
      this.flowsData = this.filterPipe.transform(this.originalFlowsData, { engFlowDisplayName: this.searchString });
      this.startIndex = 0;
    }
  }

  clearSearhInput() {
    this.searchTerm.engFlowName = '';
    this.searchDiv = false;
    this.flowsData = this.originalFlowsData;
    this.page = 1;
    this.startIndex = 0;
    this.endIndex = this.pageSize;
  }

  public sortFlow(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.flowsData = this.flowsData.sort((a, b) => {
        var term_order = a.engFlowDisplayName.localeCompare(b.engFlowDisplayName);
        return term_order;
      });
    } else {
      this.flowsData = this.flowsData.sort((a, b) => {
        var term_order = b.engFlowDisplayName.localeCompare(a.engFlowDisplayName);
        return term_order;
      });
    }
  }

  public onPageChange(currentPage: number): void {
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }
}
