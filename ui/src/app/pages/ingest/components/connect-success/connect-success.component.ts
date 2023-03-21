import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DbConnectorService } from '../../services/db-connector.service';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Subject } from 'rxjs';
import { FormGroup, FormBuilder, Validators, FormArray, FormControl } from '@angular/forms';
import { SourceDataService } from '../../services/source-data.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-connect-success',
  templateUrl: './connect-success.component.html',
  styleUrls: ['./connect-success.component.scss']
})

export class ConnectSuccessComponent implements OnInit {
  @Input() tables = [];
  @Input() connectionParams: any;

  public fg: FormGroup;
  public selectedTable: string;
  public loading: boolean;
  public folders: any;
  public originalFileColumns: string;
  public currentFileColumns: string;
  public duplicateFileColumns: string;
  public error: string;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    public modal: NgbActiveModal,
    private connectorService: DbConnectorService,
    private snackbar: SnackbarService,
    private sourceDataService: SourceDataService,
    private formBuilder: FormBuilder,
    private router: Router,
  ) { }

  ngOnInit(): void {
    const projectId = this.connectionParams.projectId;
    const userId = this.connectionParams.userId;

    this.sourceDataService.getFolders(projectId, userId).subscribe(res => {
      this.loading = false;
      this.folders = res;
    }, () => {
      this.loading = false;
    });

    this.fg = this.formBuilder.group({
      folderId: ['', Validators.required],
      checkArray: this.formBuilder.array([], [Validators.required])
    });
  }

  onCheckboxChange(e) {
    const checkArray: FormArray = this.fg.get('checkArray') as FormArray;

    if (e.target.checked) {
      checkArray.push(new FormControl(e.target.value));
    } else {
      let i = 0;
      checkArray.controls.forEach((item: FormControl) => {
        if (item.value === e.target.value) {
          checkArray.removeAt(i);
          return;
        }
        i++;
      });
    }
  }

  public onFolderChange(): void {
    this.error = this.originalFileColumns = this.currentFileColumns = '';
  }

  download(): void {
    this.loading = true;
    const folderId = this.fg.value.folderId;
    const flder = this.folders.find(x => x.folderId === +folderId);

    this.selectedTable = this.fg.value.checkArray[0];
    const conParams: any = this.connectionParams;
    conParams.tableName = this.selectedTable;
    conParams.folderId = folderId;
    conParams.hostAddress = conParams.hostName;
    this.connectorService
      .downloadTable(conParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        this.snackbar.open(res.message);
        if (res.code === 200) {
          this.modal.close('success');
          this.router.navigate([`projects/${conParams.projectId}/ingest/source-data`], {
            queryParams: { folderId, name: flder.folderName }
          });


        } else {
          this.modal.dismiss('Error');
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
