import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { FolderService } from './services/folder.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ViewDatasetComponent } from './components/view-dataset/view-dataset.component';


@Component({
  selector: 'app-file-profile',
  templateUrl: './file-profile.component.html',
  styleUrls: ['./file-profile.component.scss']
})
export class FileProfileComponent implements OnInit, OnDestroy {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  public folderId: number;
  public fileId: number;
  public projectName: string;
  public userId: number;
  public projectId: number;
  public selectedFolderName: string;
  public selectedFileName: string;
  public selectedTableName: string;
  public selectedDisplayFolderName: string;
  public foldersData: boolean;

  public folders = [];
  public pipelines = [];
  public files = [];
  public loading: boolean;

  public sourceData: boolean = false;
  public isCheck: boolean = true;

  public queryFolders: boolean = false;

  public noData: boolean;
  public sourceType: string;

  constructor(
    private router: Router,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private folderService: FolderService,
    private modalService: NgbModal,) {

    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');

    this.activatedRoute.queryParams.subscribe(params => {
      this.sourceType = params['sourcetype'];
      this.folderId = +params['folderId'];
      this.fileId = +params['fileId'];
      this.queryFolders = this.sourceType === 'pipeline' ? true : false;
    });

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.projectName = localStorage.getItem('projectname');
    // get inital data
    if(this.sourceType == 'pipeline'){
      this.getPipelinesData();
    } else {
      this.getFolderPipelineData();
    }
    
  }

  public getFolderPipelineData(): void {
    this.loading = true;
    this.folderService.getFoldersPipelines(this.projectId, this.userId, this.queryFolders)
    .pipe(takeUntil(this.unsubscribe))
    .subscribe((res) => {
     
      this.folders = res?.result;
      if (this.folders.length) {
        this.noData = false;
        this.foldersData = true;
        this.loading = false;
        const folder = this.folders.find(x => x.folderId == this.folderId) ?? this.folders[0];
        this.folderId = folder.folderId;
        this.folderName = folder.folderDisplayName;
        this.selectedDisplayFolderName = folder.folderDisplayName;

        this.folderService.getFiles(this.projectId, this.userId, this.folderId)
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

          if (this.files.length) {
            const file = this.files.find(x => x.fileId === this.fileId) ?? this.files[0];
            this.fileId = file.fileId;
            this.fileName = file.fileName;
          }
          this.redirectToDataProfile();
        });
      } else {
        this.noData = true;
        this.loading = false;
      }
    });
  }

  public getFolders(): void {
    this.folderService.getFolders(this.projectId, this.userId)
    .pipe(takeUntil(this.unsubscribe))
    .subscribe((res) => {
      this.folders = res?.result;
    });
  }

  public getFiles(): void {
    this.folderService.getFiles(this.projectId, this.userId, this.folderId)
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

      if (this.files.length) {
        this.fileId = this.files[0].fileId;
      }
      this.selectFile();
    });
  }

  public selectFolder(event: any): void {
    this.files = [];
    this.fileId = null;
    this.folderName = this.folders.find(x => x.folderId === this.folderId)?.folderDisplayName;
    this.selectedDisplayFolderName = this.folderName;

    // get files based on selected folder
    this.getFiles();
  }

  public selectPipeline(event: any): void {
    this.files = [];
    this.fileId = null;
    this.folderName = this.pipelines.find(x => x.folderId === this.folderId)?.folderDisplayName;
    this.selectedDisplayFolderName = this.folderName;

    // get files based on selected folder
    this.getFiles();
  }

  public selectFile(): void {
    this.fileName = this.files.find(x => x.fileId === this.fileId)?.fileName;
    if (this.router.url.includes('data-profile')) {
      this.redirectToDataProfile();
    } else {
      // this.redirectToDataQuality();
    }
  }

  public redirectToDataProfile(): void {
    if (this.queryFolders == false) {
      this.sourceType = 'folder';
    } else {
      this.sourceType = 'pipeline';
    }
    this.router.navigate([`projects/${this.projectId}/delta/data-profile`], {
      queryParams: {
        folderId: this.folderId,
        fileId: this.fileId,
        sourcetype: this.sourceType
      }
    });
  }

  // public redirectToDataQuality(): void {
  //   this.router.navigate([`projects/${this.projectId}/delta/data-quality`], {
  //     queryParams: {
  //       folderId: this.folderId,
  //       fileId: this.fileId
  //     }
  //   });
  // }

  public isLinkActive(url): boolean {
    const queryParamsIndex = this.router.url.indexOf('?');
    const baseUrl = queryParamsIndex === -1 ? this.router.url : this.router.url.slice(0, queryParamsIndex);
    return baseUrl.includes(url);
  }

  set folderName(val: string) {
    this.selectedFolderName = val?.length > 15 ? val : '';
  }

  set fileName(val: string) {
    this.selectedFileName = val?.length > 15 ? val : '';
    this.selectedTableName = this.selectedFileName.split('.').slice(0, -1).join('.');
  }

  public getPipelinesData(): void {
    this.loading = true;
    this.queryFolders = true;
    this.folderService.getFoldersPipelines(this.projectId, this.userId, this.queryFolders)
    .pipe(takeUntil(this.unsubscribe))
    .subscribe((res) => {
      this.loading = false;
      this.pipelines = res?.result;
      if (this.pipelines.length) {
        this.noData = false;
        this.foldersData = true;
        const pipeline =  this.pipelines.find(x => x.folderId === this.folderId) ?? this.pipelines[0];
        this.folderId = pipeline.folderId;
        this.folderName = pipeline.folderDisplayName;
        this.selectedDisplayFolderName = pipeline.folderDisplayName;

        this.folderService.getFiles(this.projectId, this.userId, this.folderId)
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

          if (this.files.length) {
            const file = this.files.find(x => x.fileId === this.fileId) ?? this.files[0];
            this.fileId = file.fileId;
            this.fileName = file.fileName;
          }
          this.redirectToDataProfile();
        });
      } else {
        this.noData = true;
        this.folderId = null;
        this.fileId = null;
      }
    });
  }

  public onItemChange(value: string): void {
    if(this.queryFolders === true){
      this.files = [];
      this.queryFolders = true;
      this.fileId = null;
      this.folderId = null;
      this.getPipelinesData();

    } else {
      this.files = [];
      this.queryFolders = false;
      this.fileId = null;
      this.folderId = null;
      this.getFolderPipelineData();
    }
  }

  viewDataset(): void {
    const modalRef: NgbModalRef = this.modalService.open(ViewDatasetComponent, { size: 'lg' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.fileId = this.fileId;
    modalRef.result.then((res) => {
    }, (reason) => {
      console.log(reason);
    });
  }

  prepareData() {
    localStorage.setItem('cleansing-selected-folder-name', this.selectedDisplayFolderName);
    localStorage.setItem('cleansing-selected-file-name', this.selectedFileName);
    this.router.navigate([`projects/${this.projectId}/cleansing/${this.folderId}/${this.fileId}`]);
  }

  ngOnDestroy() {
    this.unsubscribe.unsubscribe();
  }
}
