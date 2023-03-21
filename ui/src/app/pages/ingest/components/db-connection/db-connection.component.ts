import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { DbConnectorService } from '../../services/db-connector.service';
import { takeUntil } from 'rxjs/operators';
import { Subject, Observable } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Router, ActivatedRoute } from '@angular/router';
import { DbPipeLinePayload } from '../../models/dbpipeline';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-db-connection',
  templateUrl: './db-connection.component.html',
  styleUrls: ['./db-connection.component.scss']
})
export class DbConnectionComponent implements OnInit {
  public loading: boolean;
  fg: FormGroup;
  pipelineData = [];
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  userId: number;
  projectId: number;

  private unsubscribe: Subject<void> = new Subject<void>();
  private dbPipeLinePayload: DbPipeLinePayload;

  constructor(
    private fb: FormBuilder,
    private snackbar: SnackbarService,
    private connectorService: DbConnectorService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private dataservice: DataService
  ) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +certificate.user_id;
      });

    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
  }

  ngOnInit(): void {
    this.dataservice.updateProgress(20);

    this.fg = this.fb.group({
      insertPipelineName: ['', Validators.required]
    })

    this.getPipelineData();
  }

  public openDatabaseConnection(): void {
    if (this.fg.valid) {
      this.dbPipeLinePayload = {
        connectorId: 0,
        pipeLineName: this.fg.value.insertPipelineName,
        connectorType: true
      };

      sessionStorage.setItem('dbPipeLinePayload', JSON.stringify(this.dbPipeLinePayload));
      this.router.navigate([`projects/${this.projectId}/ingest/configure-pipeline`]);
    } else {
      this.snackbar.open("Please insert the pipeline name");
    }
  }

  public openExistingConnection(): void {
    if (this.fg.valid) {
      this.dbPipeLinePayload = {
        connectorId: 0,
        pipeLineName: this.fg.value.insertPipelineName,
        connectorType: false
      };
      sessionStorage.setItem('dbPipeLinePayload', JSON.stringify(this.dbPipeLinePayload));
      this.router.navigate([`projects/${this.projectId}/ingest/configure-pipeline`]);
    } else {
      this.snackbar.open("Please insert the pipeline name");
    }
  }

  private getPipelineData(): void {
    this.loading = true;
    this.connectorService.getPipelineData(this.projectId, this.userId)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        if (res?.code === 200) {
          this.pipelineData = res.result;
        }
        this.loading = false;
      }, (error) => {
        this.loading = false;
      });
  }
}
