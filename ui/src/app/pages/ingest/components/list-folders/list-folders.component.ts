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
    }, () => {
      this.loading = false;
    });
  }

  editFolderModal(folder): void {
    // this.edit.emit(folder);
    this.folder = folder;
    this.editFolder = true;
  }

  deleteFolder(folder: any): void {
    if (confirm("Are you sure to delete " + folder.folderDisplayName)) {
      this.loading = true;
      this.sourceDataService.deleteFolder(+this.projectId, +this.certificateData.user_id, folder.folderId).subscribe((res) => {
        this.loading = false;
        if (res?.code === 200) {
          const idx = this.folders.findIndex(x => x.folderId === folder.folderId);
          this.folders.splice(idx, 1);
          this.foldersList();
          this.snakbar.open(res?.message);
        }
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });
    }

  }

  select(folder) {
    const folderId = folder.folderId;
    const folderName = folder.folderName;
    this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
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
}
