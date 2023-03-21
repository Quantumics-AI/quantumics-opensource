import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { FoldersService } from '../../services/folders.service';
import { SourceDataService } from '../../services/source-data.service';

@Component({
  selector: 'app-connect-to-api',
  templateUrl: './connect-to-api.component.html',
  styleUrls: ['./connect-to-api.component.scss']
})
export class ConnectToApiComponent implements OnInit {

  projectId: number;

  public folders$: Observable<any>;
  public fg: FormGroup;
  public loading: boolean;
  public originalFileColumns: string;
  public currentFileColumns: string;
  public duplicateFileColumns: string;
  public error: string;

  private folders: any;
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    public modal: NgbActiveModal,
    private fb: FormBuilder,
    private quantumFacade: Quantumfacade,
    private folderService: FoldersService,
    private sourceDataService: SourceDataService,
    private snackBar: SnackbarService,
    private router: Router,
  ) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });

    this.fg = this.fb.group({
      folderId: ['', Validators.required],
      apiEndPointUrl: ['', Validators.required],
      entity: ['', Validators.required],
      apiKey: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loading = true;
    const userId = +this.certificateData.user_id;
    this.folders$ = this.sourceDataService.getFolders(this.projectId, userId).pipe(map((res) => {
      this.loading = false;
      this.folders = res;
      return res;
    }, () => {
      this.loading = false;
    }));
  }

  public onFolderChange(): void {
    this.error = this.originalFileColumns = this.currentFileColumns = '';
  }

  public download(): void {
    this.loading = true;
    const d = {
      projectId: this.projectId,
      folderId: this.fg.value.folderId,
      entity: this.fg.value.entity,
      apiKey: this.fg.value.apiKey,
      fileName: this.fg.value.entity,
      apiEndPoint: this.fg.value.apiEndPointUrl,
    };

    this.folderService.downloadAPIFiles(d).subscribe((res) => {
      this.loading = false;
      const folderId = +this.fg.value.folderId;
      const folder = this.folders.find(x => x.folderId === folderId);
      if (res.code === 200) {
        this.modal.close();
        this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
          queryParams: { folderId, name: folder.folderName }
        });
      } else {
        this.snackBar.open(`${res.message}`);
      }
    }, (error) => {
      this.loading = false;
      this.error = error?.ERROR_MSG;
      if (error.hasOwnProperty('ORIGINAL_COL_HEADERS')) {
        this.originalFileColumns = error?.ORIGINAL_COL_HEADERS.split(',');
      }
      if (error.hasOwnProperty('CURRENT_FILE_COL_HEADERS')) {
        this.currentFileColumns = error?.CURRENT_FILE_COL_HEADERS.split(',');
      }
      if (error.hasOwnProperty('INVALID_COLS')) {
        this.duplicateFileColumns = error?.INVALID_COLS.split(',');
      }
    });
  }
}
