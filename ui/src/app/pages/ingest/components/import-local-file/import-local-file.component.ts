import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { FoldersService } from '../../services/folders.service';
import { SourceDataService } from '../../services/source-data.service';

@Component({
  selector: 'app-import-local-file',
  templateUrl: './import-local-file.component.html',
  styleUrls: ['./import-local-file.component.scss']
})
export class ImportLocalFileComponent implements OnInit {
  @Input() projectId: number;
  @Input() folderId: number;
  @Input() folderName: string;

  public folders$: Observable<any>;
  public fg: FormGroup;
  // public folderId: number;
  public fileName = 'Choose file of .csv / .txt / .xls / .xlsx format.';
  public error = '';
  public originalFileColumns: any;
  public currentFileColumns: any;
  public duplicateFileColumns: any;
  public loading: boolean;
  public folders = [];
  public fileSize: number;

  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    public modal: NgbActiveModal,
    private fb: FormBuilder,
    private router: Router,
    private quantumFacade: Quantumfacade,
    private folderService: FoldersService,
    private sourceDataService: SourceDataService,
    private snackBar: SnackbarService) {

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });

    this.fg = this.fb.group({
      folderId: [{ value: this.folderId, disabled: true }, Validators.required]
    });
  }

  ngOnInit(): void {

    const userId = +this.certificateData.user_id;
    this.folders$ = this.sourceDataService.getFolders(this.projectId, userId).pipe(map((res) => {
      this.loading = false;
      this.folders = res;
      return res;
    }, () => {
      this.loading = false;
    }));
  }

  public validate(event) {
    this.error = this.originalFileColumns = this.currentFileColumns = '';

    if (!event.target.value) {
      this.fileName = 'Choose file of .csv / .txt / .xls / .xlsx format';
      return;
    }

    const regex = new RegExp('(.*?).(csv|txt|xls|xlsx)');
    if (!regex.test(event.target.value.toLowerCase())) {
      event.target.value = '';
      this.snackBar.open('Please select correct file format');
      return;
    }

    const fullFileName = event.target.value?.replace(/^.*[\\\/]/, '');
    const fileName = fullFileName?.split('.').slice(0, -1).join('.');

    if (fileName.length > 100) {
      event.target.value = '';
      this.snackBar.open('File name must be less than or equal to 100 characters');
      return;
    }

    this.fileName = event.target?.files[0]?.name;
    this.fileSize = event.target?.files[0]?.size;
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

  public onSelectFolder() {
    const folder = this.folders.find(x => x.folderId === this.folderId);
    this.folderName = folder.folderDisplayName;
    this.error = this.originalFileColumns = this.currentFileColumns = '';
  }

  public import(event): void {
    if (!event.files.length) {
      this.snackBar.open('Please select valid file.');
      return;
    }

    this.loading = true;
    const file = event.files[0];
    const userId = +this.certificateData.user_id;

    this.folderService.uploadIncremental(userId, this.projectId, this.folderId, file).subscribe((response) => {
      this.loading = false;
      this.modal.close();
      this.snackBar.open('Your file is being processed');
      // Redirect to ingest router.
      // this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
      //   queryParams: { folderId: this.folderId, name: this.folderName }
      // });

      this.router.navigate([`projects/${this.projectId}/ingest/folders/dataset`], {
        queryParams: { folderId: this.folderId, name: this.folderName, dataSourceType: 'file' }
      });

    }, (error) => {
      this.loading = false;
      this.error = error?.ERROR_MSG;
      if (error.hasOwnProperty('ORIGINAL_COL_HEADERS')) {
        this.originalFileColumns = error?.ORIGINAL_COL_HEADERS.split(',');
      } else if (error.hasOwnProperty('CURRENT_FILE_COL_HEADERS')) {
        this.currentFileColumns = error?.CURRENT_FILE_COL_HEADERS.split(',');
      } else if (error.hasOwnProperty('INVALID_COLS')) {
        this.duplicateFileColumns = error?.INVALID_COLS.split(',');
      } else {
        this.snackBar.open(`${error}`);
      }
    });
  }
}
