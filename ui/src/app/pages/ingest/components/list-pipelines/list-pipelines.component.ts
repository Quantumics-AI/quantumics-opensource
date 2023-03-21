import { Component, OnInit, EventEmitter, Output, Input } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ImportLocalFileComponent } from '../import-local-file/import-local-file.component';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { retry, takeUntil } from 'rxjs/operators';
import { DbConnectorService } from '../../services/db-connector.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { UpdatePipelineComponent } from "../update-pipeline/update-pipeline.component"
import { PipelineHistoryComponent } from "../pipeline-history/pipeline-history.component";

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
  pipeLineFolders: any;
  total_pipeline_length: number;
  searchTerm: any = { pipelineName: '' };
  showCard: boolean = true;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private sourceDataService: DbConnectorService,
    private snakbar: SnackbarService
  ) {
    // this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
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
    // getPipelineData
    this.projectId = localStorage.getItem('project_id');
    this.pipelineList()
  }

  pipelineList() {
    this.loading = true;
    this.sourceDataService.getPipelineData(+this.projectId, this.userId).subscribe((res) => {
      this.loading = false;
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
    });
  }

  public getPipeLineFolderDetails(pipeline: any): void {
    pipeline.isExpanded = !pipeline.isExpanded;
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
      }
    });
  }

  public redirectToFolder(pipeline: any): void {
    this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
      queryParams: { folderId: pipeline.qsFolders.folderId, name: pipeline.qsFolders.folderName, pipelineId: pipeline.pipelineId, dataSourceType: 'pipeline' }
    });
  }

  openEditPipeline(pipe: any): void {
    // this.folderId = job.folderId;
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
  // ImportDbComponent

  executePipeline(pipelineId: number): void {

    this.loading = true;
    this.sourceDataService.getPipelineExecuteData(+this.projectId, +this.userId, pipelineId).subscribe((res) => {
      this.loading = false;
      this.snakbar.open(res.message);
    });

    // const modalRef = this.modalService.open(ImportDbComponent, { size: 'lg', windowClass: 'modal-size' });
    // modalRef.componentInstance.projectId = this.projectId;
    // modalRef.componentInstance.userId = this.userId;
    // modalRef.componentInstance.pipelineId = pipelineId;

    // modalRef.result.then((result) => {
    //   console.log(result);
    // }, (reason) => {
    //   console.log(reason);
    // });
  }

  // Open Pipeline History component
  openHistoryPipeline(pipelineId: number): void {
    const modalRef = this.modalService.open(PipelineHistoryComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.pipelineId = pipelineId;
    // modalRef.componentInstance.id = job.folderId;

    modalRef.result.then((result) => {
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }

  deletePipeline(d: number): void {
    if (confirm("Are you sure to delete this pipeline")) {
      this.loading = true;
      this.sourceDataService.deletePipeline(+this.projectId, this.userId, d).subscribe((res) => {
        this.loading = false;
        if (res.code === 200) {
          this.pipelineList();
          this.snakbar.open(res.massage);
        } else {
          this.snakbar.open(res.message);
        }

      })
    }

  }

  select(pipe) {
    const folderId = pipe.pipelineId;
    const folderName = pipe.pipelineName;
    this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
      queryParams: { folderId, name: folderName }
    });
  }

  public selectSourceType(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/select-source-type`]);
  }

}
