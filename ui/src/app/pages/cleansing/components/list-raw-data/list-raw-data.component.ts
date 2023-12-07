import { Component, OnInit, Input } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Router, ActivatedRoute } from '@angular/router';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { SourceDataService } from 'src/app/pages/ingest/services/source-data.service';
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';

@Component({
  selector: 'app-list-raw-data',
  templateUrl: './list-raw-data.component.html',
  styleUrls: ['./list-raw-data.component.scss']
})
export class ListRawDataComponent implements OnInit {
  @Input() projectId: string;

  public folders = [];
  public pipelines = [];
  public files = [];
  public tablesFiles = [];
  public folderId: any;
  public fileId: number;
  public tableFileId: number;
  public loading: boolean;
  public folderName: string;
  public selectedFolderName: string;
  public selectedFile: string;

  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private userId: number;

  public isQueryFolder: boolean = false;

  private unsubscribe: Subject<void> = new Subject<void>();
  constructor(
    private router: Router,
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
    this.loading = true;
    this.getFolderPipelineData();
  }

  public cleansing(): void {

    const selectedFolderName = this.folders.find(x => x.folderId === this.folderId)?.folderDisplayName;
    const selectedFile = this.files.find(x => x.fileId === this.fileId)?.fileName;

    localStorage.setItem('cleansing-selected-folder-name', selectedFolderName);
    localStorage.setItem('cleansing-selected-file-name', selectedFile);
    this.router.navigate([`projects/${this.projectId}/cleansing/${this.folderId}/${this.fileId}`]);
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
    this.loading = true;
    this.folderId = null;
    this.fileId = null;
    this.tableFileId = null;
    this.folders = [];
    this.tablesFiles = [];

    this.folderService.getFoldersPipelines(+this.projectId, this.userId, this.isQueryFolder)
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
    this.loading = true;
    this.folderId = null;
    this.fileId = null;
    this.tableFileId = null;
    this.folders = [];

    this.folderService.getFoldersPipelines(+this.projectId, this.userId, this.isQueryFolder)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((res) => {
        this.folders = res?.result;
        this.loading = false;

        // if (this.folders.length) {
        //   const pipeline = this.folders.find(x => x.folderId === this.folderId);
        //   // this.folderId = pipeline.folderId;
        //   // this.folderName = pipeline.folderDisplayName;
        //   // this.selectedFolderName = pipeline.folderDisplayName;

        //   // this.folderService.getFiles(+this.projectId, this.userId, this.folderId)
        //   //   .pipe(takeUntil(this.unsubscribe))
        //   //   .subscribe((res) => {
        //   //     this.loading = false;
        //   //     this.files = res?.result;
        //   //     // Sorting files list in descending order (sort by createdDate)
        //   //     if (this.files.length > 1) {
        //   //       this.files.sort((val1, val2) => {
        //   //         return (
        //   //           (new Date(val2.createdDate) as any) -
        //   //           (new Date(val1.createdDate) as any)
        //   //         );
        //   //       });
        //   //     }

        //   //     if (this.files.length) {
        //   //       const file = this.files.find(x => x.fileId === this.fileId) ?? this.files[0];
        //   //       this.fileId = file.fileId;
        //   //       this.selectedFile = file.fileName;
        //   //     }
        //   //   }, () => {
        //   //     this.loading = false;
        //   //   });
        // } else {
        //   this.folderId = null;
        //   this.fileId = null;
        //   this.loading = false;
        // }
      }, () => {
        this.loading = false;
      });
  }

  public onItemChange(value: string): void {
    if (this.isQueryFolder) {
      this.files = [];
      this.getPipelinesData();

    } else {
      this.files = [];
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
        // if (this.files.length) {
        //   this.fileId = this.files[0].fileId;
        // }
      });
  }
}
