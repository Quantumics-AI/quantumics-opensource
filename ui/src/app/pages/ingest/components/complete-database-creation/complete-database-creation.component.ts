import { Component, OnInit } from '@angular/core';
import { PiiDataResult } from '../../models/pii';
import { FoldersService } from '../../services/folders.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { ActivatedRoute, Router } from '@angular/router';
import { DbConnectorService } from "../../services/db-connector.service";
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { DatabasePayload } from '../../models/database';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-complete-database-creation',
  templateUrl: './complete-database-creation.component.html',
  styleUrls: ['./complete-database-creation.component.scss']
})
export class CompleteDatabaseCreationComponent implements OnInit {

  public loading: boolean;
  public isVisiablePii: boolean;
  public piiData: Array<PiiDataResult>;
  public completePipeObject: any;

  private unsubscribe: Subject<void> = new Subject<void>();
  private certificate$: Observable<Certificate>;
  private userId: number;
  private projectId: number;
  private dataSourceType: string;
  private connectionParams: DatabasePayload;

  constructor(
    private quantumFacade: Quantumfacade,
    private folderService: FoldersService,
    private snackBar: SnackbarService,
    private sourceDataService: DbConnectorService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private dataservice: DataService) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });

    this.dataSourceType = this.activatedRoute.snapshot.paramMap.get('sourceType');
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
    this.piiData = JSON.parse(sessionStorage.getItem('piiResult')) as Array<PiiDataResult>;
    this.connectionParams = JSON.parse(sessionStorage.getItem('connectionParams')) as DatabasePayload;
  }

  ngOnInit(): void {
    this.dataservice.updateProgress(100);
  }

  public upload(): void {

    window.scroll({
      top: 0,
      left: 0,
      behavior: 'smooth'
    });

    this.loading = true;
    let databaseObjectRequest = [];
    // const databaseObjectRequest = [];
    const folder = this.piiData[0].folder;
    const folderName = folder?.folderName ?? '';
    const encryptPiiColumns = '';

    this.piiData.forEach(item => {
      const dropColumns = item.pii.filter(x => x.piiMaskActionType === 'Don\'t Ingest')?.map(t => t.column)?.join(',');

      databaseObjectRequest.push({
        csvFilePath: item.CSV_FILE_PATH,
        dropColumns: dropColumns,
        encryptPiiColumns: encryptPiiColumns,
        folderId: item.folder.folderId
      });
    });

    this.completePipeObject = {
      projectId: this.projectId,
      pipelineId: this?.connectionParams?.pipelineId,
      userId: this.userId,
      piiData: databaseObjectRequest
    };

    if (this.dataSourceType === 'pgsql') {
      this.sourceDataService
        .completeDB(this.completePipeObject)
        .pipe(takeUntil(this.unsubscribe))
        .subscribe(res => {
          this.loading = false;
          this.removeSessionData();
          this.snackBar.open(res.result);
          this.router.navigate([`projects/${this.projectId}/ingest/pipelines`]);
        }, (error) => {
          this.snackBar.open(error);
          this.loading = false;
        });
    } else {
      this.loading = true;
      const request = databaseObjectRequest[0];
      this.folderService.uploadPiiFile(this.userId, this.projectId, +folder.folderId, request).subscribe((response) => {
        this.loading = false;
        this.snackBar.open(response.message);
        if (response.code === 200) {
          this.removeSessionData();
          // Redirect to ingest router.
          this.router.navigate([`projects/${this.projectId}/ingest/source-data`], {
            queryParams: { folderId: folder.folderId, name: folderName }
          });
        }
      }, (error) => {
        this.loading = false;
        this.snackBar.open(error);
      });
    }
  }

  private removeSessionData(): void {
    // Remove all the created local session data
    sessionStorage.removeItem('connectionParams');
    sessionStorage.removeItem('rowPii');
    sessionStorage.removeItem('piiResult');
    sessionStorage.removeItem('dbPipeLinePayload');
    sessionStorage.removeItem('connectionParams');
    sessionStorage.removeItem('folder');
  }

  ngOnDestroy() {
  }
}
