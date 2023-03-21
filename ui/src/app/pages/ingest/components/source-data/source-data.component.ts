import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { FoldersService } from '../../services/folders.service';
import { DbConnectorService } from '../../services/db-connector.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-source-data',
  templateUrl: './source-data.component.html',
  styleUrls: ['./source-data.component.scss']
})
export class SourceDataComponent implements OnInit {

  @ViewChild('myModal', { static: false }) myModal: ElementRef;
  public projectId: number;
  private userId: number;
  private folderId: any;
  public folderName: any;
  public projectName: string;
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject<void>();
  public referesh = false;
  public loading = false;
  public sourceTypes = [];
  public dataSourceType: string;
  public searchTerm: any = { folderDisplayName: '' };
  private pipelineId: number;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private foldersService: FoldersService,
    private dbConnectorService: DbConnectorService,
    private snakbar: SnackbarService
  ) {

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });

    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
  }

  ngOnInit(): void {

    this.sourceTypes.push(
      {
        id: 0,
        name: 'Local Files',
        isExpanded: false,
        bgColor: '#c1e4ff',
        data: []
      },
      {
        id: 1,
        name: 'Pipelines',
        isExpanded: false,
        bgColor: '#87cbff',
        data: []
      });

    this.projectName = localStorage.getItem('projectname');

    this.activatedRoute.queryParams.subscribe(params => {
      this.folderName = params.name;
      this.folderId = params.folderId;
      this.pipelineId = params.pipelineId;
      this.dataSourceType = params.dataSourceType;
    });

    if (this.dataSourceType == 'file') {
      this.getSourceTypeData(this.sourceTypes[0]);
    } else if (this.dataSourceType == 'pipeline') {
      this.getSourceTypeData(this.sourceTypes[1]);
    }
  }

  public getFiles(folder: any) {
    this.sourceTypes[0].data.map(t => {
      t.isExpanded = false
    });

    folder.isExpanded = !folder.isExpanded;
    this.referesh = !this.referesh;
    this.loading = true;
    this.folderName = folder.folderName;
    this.folderId = folder.folderId;

    this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
      queryParams: { folderId: this.folderId, name: folder.folderName, dataSourceType: 'file' }
    });
  }

  selectSourceType(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/select-source-type/${this.folderName}`]);
  }

  back(): void {
    this.router.navigate([`projects/${this.projectId}/ingest`]);
  }

  public getSourceTypeData(source: any): void {

    this.sourceTypes.map(t => {
      if (source.id == t.id) {
        source.isExpanded = !source.isExpanded;
      } else {
        t.isExpanded = this.dataSourceType == t.name;
      }
    });  

    if (source.id === 0) {
      this.foldersService.getFolders(this.projectId, this.userId).subscribe((response: any) => {
        if (response.code === 200) {
          this.sourceTypes[0].data = response.result.filter(t => !t.external);

          if (this.sourceTypes[0].data.length) {
            this.sourceTypes[0].data.sort((val1, val2) => {
              return (
                (new Date(val2.createdDate) as any) -
                (new Date(val1.createdDate) as any)
              );
            });
          }

          this.sourceTypes[0].data.map((t, index) => {
            if (t.folderId == this.folderId) {
              t.isExpanded = true;
              this.sourceTypes[0].data.splice(index, 1);
              this.sourceTypes[0].data.unshift(t);
            }
          });
        }
      });
    }
    else if (source.id === 1) {
      this.dbConnectorService.getPipelineData(this.projectId, this.userId).subscribe((response: any) => {
        if (response.code === 200) {
          const pipelines = response.result;
          pipelines.sort((val1, val2) => {
            return (
              (new Date(val2.createdDate) as any) -
              (new Date(val1.createdDate) as any)
            );
          });
          this.sourceTypes[1].data = pipelines;
          this.sourceTypes[1].data.map((t, index) => {
            if (t.pipelineId == this.pipelineId) {
              t.isExpanded = true;
              this.sourceTypes[1].data.splice(index, 1);
              this.sourceTypes[1].data.unshift(t);

              t.datasetSchema.map(x => {
                if (x.qsFolders.folderId == this.folderId) {
                  this.getPipelineDetails(t);
                }
              });
            }
          });
        }
      }, () => {
        this.sourceTypes[1].data = [];
      });
    }
  }

  public getPipelineDetails(pipeline: any): void {

    this.sourceTypes[1].data.map(t => {
      if (pipeline.pipelineId == t.pipelineId) {
        t.isExpanded = !t.isExpanded;
      } else {
        t.isExpanded = false;
      }
    });

    this.dbConnectorService.getPipelineFolderData(this.projectId, this.userId, pipeline.pipelineId).subscribe((response: any) => {
      if (response.code !== 200) {
        this.snakbar.open(response.message);
      } else {
        pipeline.schemes = response.result;
      }
    }, (error) => {
      this.snakbar.open(error);
      pipeline.schemes = [];
    });
  }

  public getSelectedPipeLineFiles(pipeline: any): void {
    this.folderId = pipeline.qsFolders.folderId;
    this.folderName = pipeline.qsFolders.folderName;
    this.referesh = !this.referesh;

    this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
      queryParams: { folderId: this.folderId, name: this.folderName, pipelineId: pipeline.pipelineId, dataSourceType: 'pipeline' }
    });
  }
}
