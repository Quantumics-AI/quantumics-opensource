import { Component, OnInit, ViewChild, ElementRef, SimpleChanges, Input } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { Upload } from '../../models/upload';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { SourceDataService } from '../../services/source-data.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-list-source-data',
  templateUrl: './list-source-data.component.html',
  styleUrls: ['./list-source-data.component.scss']
})
export class ListSourceDataComponent implements OnInit {

  @Input() referesh: boolean;
  @ViewChild('myModal', { static: false }) myModal: ElementRef;
  projectId: string;
  folders = [];
  files = [];
  itemFolders = [];
  uploadModel: Upload = {} as any;
  currentUser: any;
  folderId: any;
  folderName: any;
  projectName: string;
  // tslint:disable-next-line:ban-types
  state$: Observable<Object>;
  loaded$: Observable<boolean>;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject<void>();
  sourceData$: Observable<any>;
  projectSource$: Observable<any>;
  projectParam: ProjectParam;
  folders1 = [];
  tempFiles = [];
  loading: boolean;
  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private sourceDataService: SourceDataService,
    private snakbar: SnackbarService
  ) {
    // this.loadCss()
    // this.loadScripts();
    this.loaded$ = this.quantumFacade.loaded$;
    this.projectSource$ = this.quantumFacade.projectSource$;
    this.sourceData$ = this.quantumFacade.sourceData$;

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });

    // TO-DO
    // added work around to clean the previous state. Need to find correct way
    const projectParam = {
      folderId: '-1',
    } as ProjectParam;
    this.quantumFacade.loadSourceData(projectParam);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.folderId && this.folderName) {
      this.getFiles(this.folderId, this.folderName);
    }
  }

  ngOnInit(): void {
    this.projectName = localStorage.getItem('projectname');
    this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.activatedRoute.queryParams.subscribe(params => {
      this.folderName = params.name;
      this.folderId = params.folderId;
      // this.projectId = params.projectId;
    });

    this.projectParam = {
      userId: this.certificateData.user_id,
      folderId: this.folderId,
      projectId: this.projectId,
      folderName: this.folderName,
      file: ''
    };
    console.log(this.projectParam);
    if (this.folders.length === 0) {
      this.quantumFacade.loadProjectSource(this.projectParam);
    }

    this.projectSource$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((data: any) => {
        if (data) {
          if (this.projectId) {
            this.folders = data.result;
            // Sorting folders list in descending order (sort by createdDate)
            if (this.folders.length > 1) {
              this.folders.sort((val1, val2) => {
                return new Date(val2.createdDate) as any - (new Date(val1.createdDate) as any);
              });
            }
          } else {
            const result = data.result;
            this.itemFolders = data.result;
            result.forEach(item => {
              this.projectId = item.projectId;
              this.projectParam.projectId = item.projectId;
              this.folders1 = this.folders1.concat(this.itemFolders);
              this.folders = this.folders1;
            });
          }
        } else {
          this.quantumFacade.loadProjectSource(this.projectParam);
        }
      });
    this.getFiles(this.folderId, this.folderName);

  }

  getFiles(folderId, folderName) {
    this.loading = true;
    // clear files from previous folder selected
    this.files.length = 0;
    this.folderName = folderName;
    this.folderId = folderId;
    this.projectParam.folderId = folderId;
    this.projectParam.projectId = this.projectId;
    this.quantumFacade.loadSourceData(this.projectParam);

    this.sourceData$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((data: any) => {
        this.loading = false;
        if (data) {
          if (data.result) {
            if (!Array.isArray(data.result)) {
              return;
            }
            this.files = data.result;

            // Sorting files list in descending order (sort by createdDate)
            if (this.files.length > 1) {
              this.files.sort((val1, val2) => {
                return (
                  (new Date(val2.createdDate) as any) -
                  (new Date(val1.createdDate) as any)
                );
              });
            }

            for (const file of this.files) {
              this.tempFiles.push({
                fileName: file.fileName,
                fileId: file.fileId,
                folderName: this.folderName,
                folderId: file.folderId
              });
            }
          } else {
            this.files = [];
          }
        }
      });

  }


  cleansing(filename, fileId) {
    console.log(filename);
    this.router.navigate([`../view`], {
      relativeTo: this.activatedRoute,
      queryParams: {
        folderId: this.folderId,
        folderName: this.folderName,
        file: filename,
        projectId: this.projectId,
        fId: fileId
      }
    });
  }

  delete(fileId: number, selectedFileName: string): void {
    if (confirm("Are you sure to delete " + selectedFileName)) {
      this.loading = true;
      this.sourceDataService.deleteFile(+this.projectId, +this.certificateData.user_id, this.folderId, fileId).subscribe((res) => {
        this.loading = false;
        if (res?.code == 200) {
          const idx = this.files.findIndex(x => x.fileId == fileId);
          this.files.splice(idx, 1);
          this.snakbar.open(res?.message);
        }
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });
    }

  }
}
