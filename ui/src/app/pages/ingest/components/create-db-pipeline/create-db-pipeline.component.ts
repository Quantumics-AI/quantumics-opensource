import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { DbConnectorService } from '../../services/db-connector.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';
import { takeUntil } from 'rxjs/operators';
import { Subject, Observable } from 'rxjs';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Router, ActivatedRoute } from '@angular/router';
import { DbPipeLinePayload } from '../../models/dbpipeline';
import { DatabasePayload, DbConnectorConfig } from '../../models/database';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-create-db-pipeline',
  templateUrl: './create-db-pipeline.component.html',
  styleUrls: ['./create-db-pipeline.component.scss']
})
export class CreateDbPipelineComponent implements OnInit {
  fg: FormGroup;
  connectionStatus = false;
  public accepted = false;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  userId: number;
  projectId: number;
  tablesList = [];
  pipelineData = [];

  public dbPipeLinePayload: DbPipeLinePayload;

  public loading: boolean;
  public selectedConnectionName: string;
  public connectorId: number;
  public connectionParams: DatabasePayload;
  private unsubscribe: Subject<void> = new Subject<void>();
  public isExisting: boolean = false;
  public connectorid: number;
  public pipelineid: number;
  public pgsqlType: string;

  constructor(
    private fb: FormBuilder,
    private quantumFacade: Quantumfacade,
    private connectorService: DbConnectorService,
    private snackbar: SnackbarService,
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
    this.dbPipeLinePayload = JSON.parse(sessionStorage.getItem('dbPipeLinePayload')) as DbPipeLinePayload;
    this.loading = true;
    this.fg = this.fb.group({
      pipelineName: [this.dbPipeLinePayload.pipeLineName, Validators.required],
      connectionName: new FormControl('', Validators.required),
      server: new FormControl('', Validators.required),
      port: new FormControl('', Validators.required),
      database: new FormControl('', Validators.required),
      username: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required),
      accepted: new FormControl(''),
    });

    this.loading = false;

    if (!this.dbPipeLinePayload.connectorType) {
      this.getPipelineData();
      this.isExisting = true;

    }
  }

  public testConnection(): void {
    this.loading = true;
    this.connectionParams = {
      pipelineName: this.fg.value.pipelineName,
      connectorName: this.fg.value.connectionName,
      connectorType: 'pgsql',
      projectId: this.projectId,
      userId: this.userId,
      schemaName: '',
      tableName: '',
      isExistingpipeline: this.isExisting,
      connectorId: this.connectorId,
      pipelineId: this.pipelineid,
      connectorConfig: {
        hostName: this.fg.value.server,
        userName: this.fg.value.username,
        password: this.fg.value.password,
        databaseName: this.fg.value.database,
        port: this.fg.value.port,
        serviceName: '',
      } as DbConnectorConfig
    } as DatabasePayload;

    this.connectorService
      .testConnection(this.connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        this.snackbar.open(res.message);
        if (res.code === 200) {
          this.connectionStatus = true;
        } else {
          this.connectionStatus = false;
        }
      }, (error) => {
        this.snackbar.open(error);
        this.connectionStatus = false;
        this.loading = false;
      });
  }

  public connect(): void {
    this.loading = true;
    this.connectorService
      .savePipeline(this.connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        this.snackbar.open(res.message);
        if (res.code === 200) {
          this.connectionParams.pipelineId = res.pipelineId;
          this.connectionParams.connectorId = res.connectionId;
          sessionStorage.setItem('connectionParams', JSON.stringify(this.connectionParams));
          this.router.navigate([`projects/${this.projectId}/ingest/select-table`]);
        }
      }, (error) => {
        this.snackbar.open(error);
        this.connectionStatus = false;
        this.loading = false;
      });
  }

  private getPipelineData(): void {
    this.loading = true;
    this.pgsqlType = "pgsql"
    this.connectorService.getPExistingipelineData(this.projectId, this.userId, this.pgsqlType)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.pipelineData = res.result;
        this.loading = false;
      }, (error) => {
        this.loading = false;
      })
  }

  public onSelectPipeline(connectorName: any): void {
    const connection = this.pipelineData.find(x => x.connectorName === connectorName);
    if (connection) {
      let obj = JSON.parse(connection.connectorConfig)
      this.fg.controls.server.setValue(obj.hostName);
      this.fg.controls.username.setValue(obj.userName);
      this.fg.controls.database.setValue(obj.databaseName);
      this.fg.controls.port.setValue(obj.port);
      this.fg.controls.password.setValue(obj.password);
      this.connectorId = connection.connectorId;
    }
  }
}
