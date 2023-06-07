import { Component, Input, OnInit, ViewChild, ElementRef, SimpleChanges, } from '@angular/core';
import {  ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Upload } from '../../models/upload';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { SourceDataService } from '../../services/source-data.service';
import { DeleteFolderDatasetComponent } from '../delete-folder-dataset/delete-folder-dataset.component';

@Component({
  selector: 'app-folder-dataset',
  templateUrl: './folder-dataset.component.html',
  styleUrls: ['./folder-dataset.component.scss']
})
export class FolderDatasetComponent implements OnInit {
  
  projectId: number;
  userId: number;
  files = [];
  folderId: any;
  folderName: any;
  projectName: string;
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
  searchTerm: any = { fileName: '' };

  public searchDiv: boolean = false;
  public searchString: string;
  public startIndex: number = 0;
  public pageSize: number = 15;
  public endIndex: number = this.pageSize;
  // public currnetPage: number = 0;
  // public totalPages: number;
  // public pager: Array<Pager> = [];

  public page = 1;

  public isDescending: boolean;


  constructor(
    private router: Router,
    private snakbar: SnackbarService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private sourceDataService: SourceDataService,
    private modalService: NgbModal,
  ) { 
    this.loaded$ = this.quantumFacade.loaded$;
    this.projectSource$ = this.quantumFacade.projectSource$;
    this.sourceData$ = this.quantumFacade.sourceData$;
    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
    // this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    // this.projectId = this.activatedRoute.snapshot.params['projectId'];
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +certificate.user_id;
      });

    // TO-DO
    // added work around to clean the previous state. Need to find correct way
    const projectParam = {
      folderId: '-1',
    } as ProjectParam;
    this.quantumFacade.loadSourceData(projectParam);
  }

  ngOnInit(): void {
    this.projectName = localStorage.getItem('projectname');
    // this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.activatedRoute.queryParams.subscribe(params => {
      this.folderName = params.name;
      this.folderId = params.folderId;
      // this.projectId = params.projectId;
    });

    // this.projectParam = {
    //   userId: this.certificateData.user_id,
    //   folderId: this.folderId,
    //   projectId: this.projectId,
    //   folderName: this.folderName,
    //   file: ''
    // };

    this.getFilesData(this.folderId, this.folderName);
  }

  public getFilesData(folderId, folderName): void {
    this.loading = true;
    this.files.length = 0;
    this.folderName = folderName;
    this.folderId = folderId;
    this.sourceDataService.getListData(this.projectId, this.userId, this.folderId).subscribe((res) => {
      this.loading = false;
      if(res.result){
        if (!Array.isArray(res.result)) {
          return;
        }
        this.files = res.result;
        // Sorting files list in descending order (sort by createdDate)
        if (this.files.length > 1) {
          this.files.sort((a, b) => b.createdDate - a.createdDate);
        }

        for (const file of this.files) {
          this.tempFiles.push({
            fileName: file.fileName,
            fileId: file.fileId,
            folderName: this.folderName,
            folderId: file.folderId
          });
        }
        // this.totalPages = this.files.length / this.pageSize;
        // this.totalPages = Math.ceil(this.totalPages);
        // for (let index = 0; index < this.totalPages; index++) {
        //   const page = {
        //     index,
        //     isActive: index == 0
        //   } as Pager;
  
        //   this.pager.push(page);
        // }
      } else {
        this.files = [];
      }
    }, () =>  {
      this.loading = false;
    })
  }

  // getFiles(folderId, folderName) {
  //   debugger
  //   this.loading = true;
  //   // clear files from previous folder selected
  //   this.files.length = 0;
  //   this.folderName = folderName;
  //   this.folderId = folderId;
  //   this.projectParam.folderId = folderId;
  //   this.projectParam.projectId = this.projectId;
  //   this.quantumFacade.loadSourceData(this.projectParam);

  //   this.sourceData$
  //     .pipe(takeUntil(this.unsubscribe))
  //     .subscribe((data: any) => {
  //       this.loading = false;
  //       if (data) {
  //         if (data.result) {
  //           if (!Array.isArray(data.result)) {
  //             return;
  //           }
  //           this.files = data.result;

  //           // Sorting files list in descending order (sort by createdDate)
  //           if (this.files.length > 1) {
  //             this.files.sort((val1, val2) => {
  //               return (
  //                 (new Date(val2.createdDate) as any) -
  //                 (new Date(val1.createdDate) as any)
  //               );
  //             });
  //           }

  //           for (const file of this.files) {
  //             this.tempFiles.push({
  //               fileName: file.fileName,
  //               fileId: file.fileId,
  //               folderName: this.folderName,
  //               folderId: file.folderId
  //             });
  //           }
  //         } else {
  //           this.files = [];
  //         }
  //       }
  //     });

  // }

  cleansing(filename, fileId) {
    console.log(filename);
    this.router.navigate([`projects/${this.projectId}/ingest/folders/view`], {
      queryParams: { 
        folderId: this.folderId,
        folderName: this.folderName,
        file: filename,
        projectId: this.projectId,
        fId: fileId
      }
    });
    // this.router.navigate([`../view`], {
    //   relativeTo: this.activatedRoute,
    //   queryParams: {
    //     folderId: this.folderId,
    //     folderName: this.folderName,
    //     file: filename,
    //     projectId: this.projectId,
    //     fId: fileId
    //   }
    // });
  }

  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
    } else {
      this.searchDiv = true;
    }
  }

  clearSearhInput() {
    this.searchTerm = { fileName: '' };
    this.searchDiv = false;
  }

  delete(fileId: number, selectedFileName: string): void {
    // if (confirm("Are you sure to delete " + selectedFileName)) {
    //   this.loading = true;
    //   this.sourceDataService.deleteFile(+this.projectId, +this.certificateData.user_id, this.folderId, fileId).subscribe((res) => {
    //     this.loading = false;
    //     if (res?.code == 200) {
    //       const idx = this.files.findIndex(x => x.fileId == fileId);
    //       this.files.splice(idx, 1);
    //       this.snakbar.open(res?.message);
    //     }
    //   }, (error) => {
    //     this.loading = false;
    //     this.snakbar.open(error);
    //   });
    // }



    const modalRef = this.modalService.open(DeleteFolderDatasetComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.fileId = fileId;
    modalRef.componentInstance.source = 'folder';

    modalRef.result.then((result) => {
      this.loading = true;
      this.sourceDataService.deleteFile(+this.projectId, +this.certificateData.user_id, this.folderId, fileId).subscribe((res) => {
        this.loading = false;
        if (res?.code == 200) {
          const idx = this.files.findIndex(x => x.fileId == fileId);
          this.files.splice(idx, 1);
          this.snakbar.open(res?.message);
          this.getFilesData(this.folderId, this.folderName)
        }
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });

      // const idx = this.files.findIndex(x => x.fileId == fileId);
      // this.files.splice(idx, 1);
      // this.getFilesData(this.folderId, this.folderName)
    }, () => {

     });

  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/folders`]);
    // this.location.back();
  }

  public formatFileSize(bytes): string {
    if (bytes < 1024) {
      return bytes + " bytes";
    } else if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(2) + " KB";
    } else if (bytes < 1024 * 1024 * 1024) {
      return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    } else if (bytes < 1024 * 1024 * 1024 * 1024) {
      return (bytes / (1024 * 1024 * 1024)).toFixed(2) + " GB";
    } else {
      return (bytes / (1024 * 1024 * 1024 * 1024)).toFixed(2) + " TB";
    }
  }

  public redirectToDF(file: any): void {
    this.router.navigate([`projects/${this.projectId}/delta/data-profile`], {
      queryParams: {
        folderId: file.folderId,
        fileId: file.fileId,
        sourcetype: 'folder'
      }
    });
  }

  public sortDataset(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.files = this.files.sort((a, b) => {
        var fileName_order = a.fileName.localeCompare(b.fileName);
        return fileName_order;
      });
    } else {
      this.files = this.files.sort((a, b) => {
        var fileName_order = b.fileName.localeCompare(a.fileName);
        return fileName_order;
      });
    }

  }

  // public previousPage(): void {
  //   this.currnetPage--;
  //   this.pager.map(t => t.isActive = false);

  //   if (this.currnetPage == 0) {
  //     this.currnetPage = 0;
  //     this.startIndex = 0;
  //     this.endIndex = this.pageSize;
  //   } else {
  //     this.startIndex = this.pageSize * this.currnetPage;
  //     this.endIndex = this.startIndex + this.pageSize;
  //   }

  //   this.pager[this.currnetPage].isActive = true;
  // }

  // public nextPage(): void {
  //   this.pager.map(t => t.isActive = false);

  //   this.currnetPage++;

  //   if (this.totalPages != this.currnetPage) {
  //     this.startIndex = this.endIndex;
  //     this.endIndex = this.startIndex + this.pageSize;
  //   }

  //   this.pager[this.currnetPage].isActive = true;
  // }

  // public redirectToPageIndex(pager: Pager): void {
  //   this.pager.map(t => t.isActive = false);
  //   pager.isActive = true;
  //   this.currnetPage = pager.index;
  //   this.startIndex = this.pageSize * this.currnetPage;
  //   this.endIndex = this.startIndex + this.pageSize;
  // }

  public onPageChange(currentPage: number): void {
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

}
