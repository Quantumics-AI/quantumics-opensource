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
import { AuthTypeEnum } from '../../enums/authtype-enum';
import { APITypeEnum } from '../../enums/apitype-enum';

@Component({
  selector: 'app-connect-to-api',
  templateUrl: './connect-to-api.component.html',
  styleUrls: ['./connect-to-api.component.scss']
})
export class ConnectToApiComponent implements OnInit {

  projectId: number;

  public fg: FormGroup;
  public loading: boolean;
  public originalFileColumns: string;
  public currentFileColumns: string;
  public duplicateFileColumns: string;
  public error: string;
  public page: number = 0;
  public searchString: string = '';
  public customHeaders: { key: string, value: string, rowIndex: number }[] = [];
  public isTestConnectionSuccessfull: boolean = false;
  public folders: Array<any>;

  public authTypEnum = AuthTypeEnum;
  public apiTypeEnum = APITypeEnum;

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
      endpoint: ['', [Validators.required, Validators.maxLength(3000), Validators.pattern(/^(ftp|http|https):\/\/[^ "]+$/)]],
      httpMethodType: ['', Validators.required],
      authType: ['', Validators.required],
      contextPath: ['', [Validators.pattern(/^[a-zA-Z0-9-_\/]*$/), Validators.maxLength(100)]],
      bearerToken: [''],
      apiKey: [''],
      apiValue: [''],
    });

    this.customHeaders.push({ key: '', value: '', rowIndex: 0 });
    const index = this.customHeaders.length - 1;

    this.fg.addControl(`customHeaderKey${index}`, this.fb.control('', Validators.maxLength(50)));
    this.fg.addControl(`customHeaderValue${index}`, this.fb.control('', Validators.maxLength(50)));
  }

  ngOnInit(): void {
    this.loading = true;
    const userId = +this.certificateData.user_id;
    this.sourceDataService.getFolders(this.projectId, userId).subscribe((response) => {
      this.loading = false;
      this.folders = response;
    }, () => {
      this.loading = false;
    });

    this.fg.valueChanges.subscribe(() => {
      this.isTestConnectionSuccessfull = false;
    });
  }

  public addCustomHeader(): void {

    if (this.customHeaders.length === 10) {
      this.snackBar.open('Only a maximum of 10 custom headers are allowed.');
      return;
    }

    if (this.customHeaders.length === 1) {
      // Make require field for default added field.

      this.fg.get(`customHeaderKey${this.customHeaders.length - 1}`).setValidators(Validators.required);
      this.fg.get(`customHeaderValue${this.customHeaders.length - 1}`).setValidators(Validators.required);

      this.fg.get(`customHeaderKey${this.customHeaders.length - 1}`)?.updateValueAndValidity();
      this.fg.get(`customHeaderValue${this.customHeaders.length - 1}`)?.updateValueAndValidity();
    }

    this.customHeaders.push({ key: '', value: '', rowIndex: this.customHeaders.length });
    const index = this.customHeaders.length - 1;

    this.fg.addControl(`customHeaderKey${index}`, this.fb.control('', Validators.required));
    this.fg.addControl(`customHeaderValue${index}`, this.fb.control('', Validators.required));
  }

  public removeCustomHeader(index: number): void {
    const rowIndex = this.customHeaders[index].rowIndex;
    this.fg.removeControl(`customHeaderKey${rowIndex}`);
    this.fg.removeControl(`customHeaderValue${rowIndex}`);

    this.customHeaders.splice(index, 1);

    if (this.customHeaders.length === 1) {
      // remove required field validation for default added field.

      this.fg.get(`customHeaderKey${this.customHeaders.length - 1}`)?.clearValidators();
      this.fg.get(`customHeaderValue${this.customHeaders.length - 1}`)?.clearValidators();

      this.fg.get(`customHeaderKey${this.customHeaders.length - 1}`)?.updateValueAndValidity();
      this.fg.get(`customHeaderValue${this.customHeaders.length - 1}`)?.updateValueAndValidity();
    }
  }

  public onFolderChange(): void {
    this.error = this.originalFileColumns = this.currentFileColumns = '';
  }

  public testConnectionOrDownload(type: APITypeEnum): void {

    const authentication = {};
    const headers = {};

    if (+this.fg.value.authType === this.authTypEnum.BEARER_TOKEN) {
      authentication['bearerToken'] = this.fg.value.bearerToken;
    } else {
      authentication[this.fg.value.apiKey] = this.fg.value.apiValue;
    }

    for (let index = 0; index < this.customHeaders.length; index++) {

      const rowIndex = this.customHeaders[index].rowIndex;

      if (index === 0) {
        if (this.fg.value[`customHeaderKey${rowIndex}`] && !this.fg.value[`customHeaderValue${rowIndex}`]) {
          this.snackBar.open('Please enter header value');
          return;
        }
        else if (!this.fg.value[`customHeaderKey${rowIndex}`] && this.fg.value[`customHeaderValue${rowIndex}`]) {
          this.snackBar.open('Please enter header key');
          return;
        }
      }

      if (!this.fg.value[`customHeaderKey${rowIndex}`] || !this.fg.value[`customHeaderValue${rowIndex}`]) {
        continue;
      }

      headers[`${this.fg.value[`customHeaderKey${rowIndex}`]}`] = this.fg.value[`customHeaderValue${rowIndex}`];
    }

    this.loading = true;

    const d = {
      projectId: this.projectId,
      userId: +this.certificateData.user_id,
      apiEndPoint: this.fg.value.endpoint,
      httpMethodType: this.fg.value.httpMethodType?.toUpperCase(),
      authType: +this.fg.value.authType === this.authTypEnum.BEARER_TOKEN ? 'BEARER_TOKEN' : 'API_KEY',
      folderId: +this.fg.value.folderId,
      authentication: { params: authentication },
      headers: headers,
      contextPath: this.fg.value.contextPath
    };

    if (type === APITypeEnum.TEST_CONNECTION) {
      this.folderService.testDownloadAPIFiles(d).subscribe((res) => {
        this.loading = false;
        this.snackBar.open(res.message);

        if (res.code === 200) {
          this.isTestConnectionSuccessfull = true;

        } else {
          this.isTestConnectionSuccessfull = false;
        }
      }, (error) => {
        this.loading = false;
        this.snackBar.open(error);
        this.isTestConnectionSuccessfull = false;
      });
    } else {
      this.folderService.downloadAPIFiles(d).subscribe((res) => {
        this.loading = false;
        this.snackBar.open(`${res.message}`);
        // const folderId = +this.fg.value.folderId;
        // const folder = this.folders.find(x => x.folderId === folderId);
        if (res.code === 200) {
          this.modal.close();
          // this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
          //   queryParams: { folderId, name: folder.folderName }
          // });
        }
      }, (error) => {
        this.loading = false;
        this.snackBar.open(error);
      });
    }
  }

  public onAuthTypeChange(authType: number): void {

    this.fg.get('bearerToken')?.reset();
    this.fg.get('apiKey')?.reset();
    this.fg.get('apiValue')?.reset();

    this.fg.get('bearerToken')?.clearValidators();
    this.fg.get('apiKey')?.clearValidators();
    this.fg.get('apiValue')?.clearValidators();


    if (+authType === this.authTypEnum.BEARER_TOKEN) {
      this.fg.get('bearerToken').setValidators([Validators.required, Validators.maxLength(300)]);
    }
    else {
      this.fg.get('apiKey').setValidators([Validators.required, Validators.maxLength(50)]);
      this.fg.get('apiValue').setValidators([Validators.required, Validators.maxLength(300)]);
    }

    this.fg.get('bearerToken')?.updateValueAndValidity();
    this.fg.get('apiKey')?.updateValueAndValidity();
    this.fg.get('apiValue')?.updateValueAndValidity();
  }
}
