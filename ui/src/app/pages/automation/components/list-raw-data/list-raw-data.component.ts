import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, OnChanges, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { SourceDataService } from 'src/app/pages/ingest/services/source-data.service';
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';

@Component({
  selector: 'app-list-raw-data',
  templateUrl: './list-raw-data.component.html',
  styleUrls: ['./list-raw-data.component.scss']
})
export class ListRawDataComponent implements OnInit, OnChanges, OnDestroy {

  @Input() projectId: string;
  @Input() reset: boolean;
  @Output() selectedDetails = new EventEmitter<any>();

  public folders = [];
  public pipelines = [];
  public files = [];
  public folderId: any;
  public fileId: number;
  public loading: boolean;
  public folderName: string;
  public selectedFolderName: string;
  public selectedFile: string;
  public tablesFiles = [];
  public tableFileId: number;

  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private userId: number;
  public isQueryFolder: boolean = false;
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private folderService: FolderService,
    private quantumFacade: Quantumfacade,
    private sourceDataService: SourceDataService
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
    if (!this.isQueryFolder) {
      this.isQueryFolder = false;
      this.getFolderPipelineData();
    } else {
      this.onItemChange();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.reset.firstChange) {
      this.folderId = null;
      this.fileId = null;
      this.tableFileId = null;
    }
  }

  public cleansing(): void {
    this.selectedDetails.emit({ folderId: this.folderId, fileId: this.fileId });
  }

  public onFolderSelect(): void {
    this.loading = true;
    this.fileId = null;
    this.tableFileId = null;

    this.sourceDataService.getFiles(+this.projectId, this.userId, this.folderId)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response) => {
        this.loading = false;
        this.files = response;

        if (this.files.length > 1) {
          this.files.sort((val1, val2) => {
            return (
              (new Date(val2.createdDate) as any) -
              (new Date(val1.createdDate) as any)
            );
          });

        }
      }, () => {
        this.loading = false;
      });
  }

  public getFolderPipelineData(): void {
    this.folderId = null;
    this.fileId = null;
    this.tableFileId = null;
    this.folders = [];


    this.loading = true;
    this.folderService.getFoldersPipelines(+this.projectId, this.userId, false)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response) => {
        this.loading = false;
        this.folders = response.result;

        if (this.folders.length > 1) {
          this.folders.sort((val1, val2) => {
            return (
              (new Date(val2.createdDate) as any) -
              (new Date(val1.createdDate) as any)
            );
          });
        }
      }, () => {
        this.loading = false;
      });
  }

  public getPipelinesData(): void {
    this.folderId = null;
    this.fileId = null;
    this.tableFileId = null;
    this.folders = [];

    this.loading = true;

    this.folderService.getFoldersPipelines(+this.projectId, this.userId, true)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((res) => {
        this.folders = res?.result;
        this.loading = false;
      }, () => {
        this.loading = false;
      });
  }

  public onItemChange(value: string = ''): void {
    this.files = [];
    this.tablesFiles = [];

    if (this.isQueryFolder) {
      this.getPipelinesData();

    } else {
      this.getFolderPipelineData();
    }
  }

  public selectPipeline(event: any): void {
    this.files = [];
    this.tablesFiles = [];
    this.fileId = null;
    this.tableFileId = null;
    this.folderName = this.folders.find(x => x.folderId === this.folderId)?.folderDisplayName;
    this.selectedFolderName = this.folderName;

    // get files based on selected folder
    this.getFiles();
  }

  public getFiles(): void {
    this.folderService.getFiles(+this.projectId, this.userId, this.folderId)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((res) => {
        this.files = res?.result;

        // Sorting files list in descending order (sort by createdDate)
        if (this.files.length > 1) {
          this.files.sort((val1, val2) => {
            return (
              (new Date(val2.createdDate) as any) -
              (new Date(val1.createdDate) as any)
            );
          });
        }

        this.tablesFiles = this.files;

        if (this.isQueryFolder) {
          this.tablesFiles = this.tablesFiles.reduce((result, file) => {
            if (!result.some(item => item.fileName === file.fileName)) {
              result.push(file);
            }
            return result;
          }, []);
        }
      });
  }

  ngOnDestroy() {
    this.unsubscribe.unsubscribe();
  }
}
