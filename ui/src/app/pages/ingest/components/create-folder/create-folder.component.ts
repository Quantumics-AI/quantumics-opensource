import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Folder } from '../../models/folder';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SourceDataService } from '../../services/source-data.service';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-create-folder',
  templateUrl: './create-folder.component.html',
  styleUrls: ['./create-folder.component.scss']
})
export class CreateFolderComponent implements OnInit {
  @Output() stpper = new EventEmitter<any>();

  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();
  private projectId: number;
  private folder: Folder;

  public fg: FormGroup;
  public loading: boolean;
  lengthErrorMsg: boolean = false;

  constructor(
    private fb: FormBuilder,
    private snakbar: SnackbarService,
    private sourceDataService: SourceDataService,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private dataservice: DataService) {

    this.dataservice.updateProgress(20);
    this.folder = new Folder();
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.certificateData = certificate;
        }
      });
  }

  ngOnInit(): void {
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');

    this.fg = this.fb.group({
      folderName: ['', [Validators.required, Validators.maxLength(30)]],
      folderDesc: ['', [Validators.maxLength(255)]],
    });

    console.log(this.fg.invalid);
    
  }

  modelChangeDesc(str) {
    if (str.length > 255) {
      this.lengthErrorMsg = true;
    } else {
      this.lengthErrorMsg = false
    }
  }

  public create(): void {
    this.loading = true;
    this.folder.folderName = this.fg.controls.folderName.value;
    this.folder.folderDesc = this.fg.controls.folderDesc.value;
    this.folder.userId = this.certificateData.user_id;
    this.folder.projectId = this.projectId?.toString();
    this.folder.dataOwner = this.certificateData.user;

    this.sourceDataService.createFolder(this.folder).subscribe((response) => {
      this.loading = false;
      if (response.code === 200) {
        this.folder.folderId = response.result.folderId;
        this.snakbar.open(response.message);
        sessionStorage.setItem('folder', JSON.stringify(response.result));
        this.router.navigate([`projects/${this.projectId}/ingest/configure-folder/`]);
      }
    }, (error) => {
      this.snakbar.open(error);
      this.loading = false;
    });
  }
}
