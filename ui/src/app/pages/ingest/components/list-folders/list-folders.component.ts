import { Component, OnInit, EventEmitter, Output, Input } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ImportLocalFileComponent } from '../import-local-file/import-local-file.component';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { SourceDataService } from '../../services/source-data.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { EditFolderComponent } from '../edit-folder/edit-folder.component';
import { FolderConfirmationComponent } from '../folder-confirmation/folder-confirmation.component';


@Component({
  selector: 'app-list-folders',
  templateUrl: './list-folders.component.html',
  styleUrls: ['./list-folders.component.scss']
})
export class ListFoldersComponent implements OnInit {
  // @Input() folders: any;
  folders: any;
  @Output() edit: EventEmitter<any> = new EventEmitter<any>();
  projectId: string;
  projectName: string;
  searchTerm: any = { folderName: '' };
  public isDescending: boolean;
  total_folder_length: number;
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private certificateData: Certificate;
  userId: number;
  loading: boolean;
  editFolder: boolean;
  folder: any;
  showCard: boolean = true;
  filteredData: any;

  public searchDiv: boolean = false;
  public searchString: string;
  public startIndex: number = 0;
  public pageSize: number = 15;
  public endIndex: number = this.pageSize;
  // public currnetPage: number = 0;
  // public totalPages: number;
  // public pager: Array<Pager> = [];

  public page = 1;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private sourceDataService: SourceDataService,
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
    this.projectId = localStorage.getItem('project_id');
    this.foldersList();
    this.projectName = localStorage.getItem('projectname');

  }

  foldersList() {
    this.loading = true;
    this.sourceDataService.getFolders(+this.projectId, this.userId).subscribe((response) => {
      this.loading = false;
      response = response.filter(t => !t.external);
      this.filteredData = response.filter(obj => {
        return obj.pipelineType == null;
      });
      this.folders = this.filteredData;
      if (this.folders.length > 0) {
        this.showCard = false;
      }
      if (this.folders.length > 1) {
        this.folders.sort((val1, val2) => {
          return (
            (new Date(val2.createdDate) as any) -
            (new Date(val1.createdDate) as any)
          );
        });
      }
      this.total_folder_length = this.folders.length;
      // this.totalPages = this.folders.length / this.pageSize;
      // this.totalPages = Math.ceil(this.totalPages);

      // for (let index = 0; index < this.totalPages; index++) {
      //   const page = {
      //     index,
      //     isActive: index == 0
      //   } as Pager;

      //   this.pager.push(page);
      // }
    }, () => {
      this.loading = false;
    });
  }

  editFolderModal(folder: any): void {
    // this.edit.emit(folder);
    // this.folder = folder;
    // this.editFolder = true;
    const modalRef = this.modalService.open(EditFolderComponent, { size: 'md' });
    modalRef.componentInstance.folder = folder;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;

    modalRef.result.then((result) => {
      this.updatedFolder(result);
    });
  }

  deleteFolder(folder: any): void {
    // if (confirm("Are you sure to delete " + folder.folderDisplayName)) {
    //   this.loading = true;
    //   this.sourceDataService.deleteFolder(+this.projectId, +this.certificateData.user_id, folder.folderId).subscribe((res) => {
    //     this.loading = false;
    //     if (res?.code === 200) {
    //       const idx = this.folders.findIndex(x => x.folderId === folder.folderId);
    //       this.folders.splice(idx, 1);
    //       this.foldersList();
    //       this.snakbar.open(res?.message);
    //     }
    //   }, (error) => {
    //     this.loading = false;
    //     this.snakbar.open(error);
    //   });
    // }

    const modalRef = this.modalService.open(FolderConfirmationComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = folder.folderId;

    modalRef.result.then((result) => {
      this.loading = true;
      this.sourceDataService.deleteFolder(+this.projectId, +this.certificateData.user_id, folder.folderId).subscribe((response: any) => {
        this.snakbar.open(response.message);
        this.loading = false;
        if (response.code === 200) {
          const idx = this.folders.findIndex(x => x.folderId === folder.folderId);
          this.folders.splice(idx, 1);
          this.foldersList()
        }
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });
      
    }, (result) => {
      
     });

  }

  select(folder) {
    const folderId = folder.folderId;
    const folderName = folder.folderName;
    // this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
    //   queryParams: { folderId, name: folderName, dataSourceType: 'file' }
    // });

    this.router.navigate([`projects/${this.projectId}/ingest/folders/dataset`], {
      queryParams: { folderId, name: folderName, dataSourceType: 'file' }
    });

  }

  public sort(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.folders = this.folders.sort((a, b) => {
        return new Date(a.createdDate) as any - <any>new Date(b.createdDate);
      });
    } else {
      this.folders = this.folders.sort((a, b) => {
        return (new Date(b.createdDate) as any) - <any>new Date(a.createdDate);
      });
    }

  }

  public sortFolder(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.folders = this.folders.sort((a, b) => {
        var folder_name_order = a.folderDisplayName.localeCompare(b.folderDisplayName);
        return folder_name_order;
      });
    } else {
      this.folders = this.folders.sort((a, b) => {
        var folder_name_order = b.folderDisplayName.localeCompare(a.folderDisplayName);
        return folder_name_order;
      });
    }

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
    this.searchTerm = { folderName: '' };
    this.searchDiv = false;
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

  uploadFile(folder: any): void {
    const modalRef = this.modalService.open(ImportLocalFileComponent, { size: 'lg' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = folder.folderId;
    modalRef.componentInstance.folderName = folder.folderDisplayName;

    modalRef.result.then((result) => {
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }

  updatedFolder(folder: any): void {
    const index = this.folders.findIndex(x => x.folderId === folder?.folderId);
    this.folders[index] = folder;
  }

  public selectSourceType(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/select-source-type`]);
  }

  public onPageChange(currentPage: number): void {
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }
}
