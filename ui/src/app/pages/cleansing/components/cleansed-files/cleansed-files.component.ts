import { Component, OnInit, Input } from '@angular/core';
import { CleansingDataService } from '../../services/cleansing-data.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FilterPipe } from 'ngx-filter-pipe';

@Component({
  selector: 'app-cleansed-files',
  templateUrl: './cleansed-files.component.html',
  styleUrls: ['./cleansed-files.component.scss']
})
export class CleansedFilesComponent implements OnInit {
  files = [];
  private originalFiles: Array<any> = [];
  loading: boolean;

  projectId: number;
  userId: number;
  folderId: any;
  folderName: any;
  projectName: string;
  fileName: string;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject<void>();

  public searchDiv: boolean = false;
  public searchString: string;
  searchTerm: any = { fileName: '' };
  public startIndex: number = 0;
  public pageSize: number = 10;
  public endIndex: number = this.pageSize;
  public page = 1;

  public isDescending: boolean;

  constructor(
    private cleansingDataService: CleansingDataService,
    // public modal: NgbActiveModal,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private snakbar: SnackbarService,
    private quantumFacade: Quantumfacade,
    private filterPipe: FilterPipe
  ) {
    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.projectName = localStorage.getItem('projectname');
    // this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.activatedRoute.queryParams.subscribe(params => {
      this.folderName = params.foldername;
      this.folderId = params.folderId;
      this.fileName = params.filename;
    });
    this.cleansingList();
  }

  public cleansingList(): void {
    this.loading = true;
    this.cleansingDataService.getAddedCleansingFiles(this.projectId, this.userId, this.folderId).subscribe(response => {
      this.loading = false;
      console.log(response);
      if (response?.code === 200) {
        this.files = response.result;

        if (this.files.length > 0) {
          this.files.map((t, index) => {
            t.index = index;
          });
        }

        this.originalFiles = [...this.files];
      }
    }, (error) => {
      this.loading = false;
    });
  }

  cleansingEditor(fileId: number, fileName: string): void {
    // this.modal.close('closed');

    localStorage.setItem('cleansing-selected-folder-name', this.folderName);
    localStorage.setItem('cleansing-selected-file-name', fileName);
    this.router.navigate([`projects/${this.projectId}/cleansing/${this.folderId}/${fileId}`]);
  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/cleansing`]);
    // this.location.back();
  }

  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
      this.files = this.originalFiles;
    } else {
      this.searchDiv = true;
      this.files = this.filterPipe.transform(this.originalFiles, { fileName: this.searchString });
      this.startIndex = 0;
    }
  }

  public onPageChange(currentPage: number): void {
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  clearSearhInput() {
    this.searchTerm = { fileName: '' };
    this.searchDiv = false;
    this.files = this.originalFiles;
    this.page = 1;
    this.startIndex = 0;
    this.endIndex = this.pageSize;
  }

  public sortFolderName(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.files = this.files.sort((a, b) => {
        var folder_name_order = a.fileName.localeCompare(b.fileName);
        return folder_name_order;
      });
    } else {
      this.files = this.files.sort((a, b) => {
        var folder_name_order = b.fileName.localeCompare(a.fileName);
        return folder_name_order;
      });
    }

  }
}
