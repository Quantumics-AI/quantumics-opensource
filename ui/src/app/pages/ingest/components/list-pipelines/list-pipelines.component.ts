import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { DbConnectorService } from '../../services/db-connector.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { UpdatePipelineComponent } from "../update-pipeline/update-pipeline.component"
import { PipelineHistoryComponent } from "../pipeline-history/pipeline-history.component";
import { PipelineConfirmationComponent } from '../pipeline-confirmation/pipeline-confirmation.component';
import { FilterPipe } from 'ngx-filter-pipe';
import { PAGE_SIZE } from 'src/app/core/const/pageSize';

@Component({
  selector: 'app-list-pipelines',
  templateUrl: './list-pipelines.component.html',
  styleUrls: ['./list-pipelines.component.scss']
})
export class ListPipelinesComponent implements OnInit {
  loading: boolean;
  public isDescending: boolean;
  projectId: string;
  userId: number;
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private certificateData: Certificate;
  pipelines: any;
  originalPipelines: any;
  pipeLineFolders: any;
  total_pipeline_length: number;
  searchTerm: any = { pipelineName: '' };
  showCard: boolean = true;
  public execute: boolean = false;
  public executeId: number = null;

  public searchDiv: boolean = false;
  public searchString: string;
  public startIndex: number = 0;
  public pageSize: number = PAGE_SIZE;
  public endIndex: number = this.pageSize;

  public page = 1;
  public Jheader: boolean = true;
  public Jfooter: boolean = false;

  private currentPage: number;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private sourceDataService: DbConnectorService,
    private snakbar: SnackbarService,
    private filterPipe: FilterPipe
  ) {
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
    this.projectId = localStorage.getItem('project_id');
    this.pipelineList()
  }

  pipelineList() {
    this.loading = true;
    this.sourceDataService.getPipelineData(+this.projectId, this.userId).subscribe((res) => {
      this.loading = false;
      if (res.code == 200) {
        this.pipelines = res.result;
        if (this.pipelines.length > 0) {
          this.showCard = false;
        }
        if (this.pipelines.length > 1) {
          this.pipelines.map(t => {
            t.isExpanded = false;
          });

          this.pipelines.sort((val1, val2) => {
            return (
              (new Date(val2.createdDate) as any) -
              (new Date(val1.createdDate) as any)
            );
          });
        }
        this.total_pipeline_length = this.pipelines.length;
      } else {
        this.pipelines = [];
      }

      this.originalPipelines = [...this.pipelines];

    }, (error) => {
      this.loading = false;
    });
  }

  public refresh(): void {
    this.pipelineList();
  }

  public getPipeLineFolderDetails(pipeline: any): void {

    this.pipelines.map(t => {
      if (t.pipelineId != pipeline.pipelineId) {
        t.isExpanded = false;
      }
    });

    this.sourceDataService.getPipelineFolderData(+this.projectId, this.userId, pipeline.pipelineId).subscribe((response: any) => {
      if (response.code !== 200) {
        this.snakbar.open(response.message);
        pipeline.isExpanded = false;
        this.pipeLineFolders = [];
      }
      else {
        this.pipeLineFolders = response.result;
        pipeline.isExpanded = !pipeline.isExpanded;
      }
    });
  }

  public redirectToFolder(pipeline: any): void {

    localStorage.setItem('selectedFolderName', pipeline.qsFolders.folderName);
    this.router.navigate([`projects/${this.projectId}/ingest/pipelines/dataset/${pipeline.pipelineId}/${pipeline.qsFolders.folderId}`]);
  }

  openEditPipeline(pipe: any): void {
    const modalRef = this.modalService.open(UpdatePipelineComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.id = pipe.pipelineId;
    modalRef.componentInstance.pipeData = pipe;

    modalRef.result.then((result) => {
      this.pipelineList();
      console.log(result);
    }, (reason) => {
      console.log(reason);
      this.pipelineList();
    });
  }
  executePipeline(pipelineId: number): void {
    this.execute = true;
    this.executeId = pipelineId;

    this.sourceDataService.getPipelineExecuteData(+this.projectId, +this.userId, pipelineId).subscribe((res) => {
      this.snakbar.open(res.message);
      this.execute = false;
      this.executeId = null;
      this.pipelineList();

    });
  }

  // Open Pipeline History component
  openHistoryPipeline(pipelineId: number): void {
    const modalRef = this.modalService.open(PipelineHistoryComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.pipelineId = pipelineId;

    modalRef.result.then((result) => {
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }

  deletePipeline(d: number): void {
    const modalRef = this.modalService.open(PipelineConfirmationComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.pipelineId = d;

    modalRef.result.then((result) => {
      this.loading = true;

      this.sourceDataService.deletePipeline(+this.projectId, this.userId, d).subscribe((response: any) => {
        this.loading = false;
        if (response.code === 200) {
          this.snakbar.open(response.massage);
          const idx = this.pipelines.findIndex(x => x.pipelineId === d);
          this.pipelines.splice(idx, 1);

          const noOfPages = this.pipelines.length / this.pageSize;
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
    }, (reason) => { 
      this.loading = false;
    });
  }

  select(pipe) {
    const folderId = pipe.pipelineId;
    const folderName = pipe.pipelineName;
    this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
      queryParams: { folderId, name: folderName }
    });
  }

  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
    } else {
      this.searchDiv = true;
      this.pipelines = this.filterPipe.transform(this.originalPipelines, { pipelineName: this.searchString });
      this.startIndex = 0;
    }
  }

  clearSearhInput() {
    this.searchTerm = { pipelineName: '' };
    this.searchDiv = false;

    this.pipelines = this.originalPipelines;
    this.page = 1;
    this.startIndex = 0;
    this.endIndex = this.pageSize;
  }

  public onPageChange(currentPage: number): void {
    this.currentPage = currentPage;
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public selectSourceType(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/select-source-type`]);
  }

  public sortPipeLine(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.pipelines = this.pipelines.sort((a, b) => {
        var pipelineName_order = a.pipelineName.localeCompare(b.pipelineName);
        return pipelineName_order;
      });
    } else {
      this.pipelines = this.pipelines.sort((a, b) => {
        var pipelineName_order = b.pipelineName.localeCompare(a.pipelineName);
        return pipelineName_order;
      });
    }
  }
}
