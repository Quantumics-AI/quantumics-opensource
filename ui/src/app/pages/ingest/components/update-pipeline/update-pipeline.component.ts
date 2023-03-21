import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Router, ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Connectors } from '../../models/dbconnectors';
import { Certificate } from 'src/app/models/certificate';
import { DbConnectorService } from '../../services/db-connector.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { DatabasePayload, DbConnectorConfig } from '../../models/database';
import { DataBaseStpperType } from '../../enums/stepper'

@Component({
  selector: 'app-update-pipeline',
  templateUrl: './update-pipeline.component.html',
  styleUrls: ['./update-pipeline.component.scss']
})
export class UpdatePipelineComponent implements OnInit {
  @Input() connectors: Connectors;
  @Input() id: number;
  @Input() pipeData: any;
  @Output() stpper = new EventEmitter<any>();

  projectId: string;
  userId: number;
  folderId: number;
  history$: Observable<any>;
  loading: boolean;
  fg: FormGroup;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  pipelinesData: any;
  connectorId: number;
  public accepted: boolean;
  private unsubscribe: Subject<void> = new Subject<void>();
  public publishedPipeline: boolean;
  public editConnectionParams: any;
  connectionStatus = false;


  constructor(
    private router: Router,
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
    private fb: FormBuilder,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private sourceDataService: DbConnectorService,
  ) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = parseInt(certificate.user_id, 10);
      });

    // this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
  }

  ngOnInit(): void {
    console.log("PIPE DATA", this.pipeData);

    this.projectId = localStorage.getItem('project_id');

    this.loading = true;
    this.fg = this.fb.group({
      pipelineName: new FormControl(this.connectors?.pipelineName, Validators.required),
      connectionName: new FormControl(this.connectors?.connectorName, Validators.required),
      server: new FormControl(this.connectors?.hostName, Validators.required),
      port: new FormControl(this.connectors?.port, Validators.required),
      database: new FormControl(this.connectors?.databaseName, Validators.required),
      username: new FormControl(this.connectors?.userName, Validators.required),
      password: new FormControl(this.connectors?.password, Validators.required),
      accepted: new FormControl(this.connectors?.saveDbCon),
      // pipelineId: new FormControl(this.connectors?.pipelineId),
    });

    this.loading = false;

    this.getPipelineData();
  }

  getPipelineData(): void {
    this.loading = true;

    if (this.pipeData) {
      this.loading = false;
      let obj = JSON.parse(this.pipeData.connectorDetails.connectorConfig);
      this.connectorId = this.pipeData.connectorDetails.connectorId;
      this.fg.controls.pipelineName.setValue(this.pipeData.pipelineName)
      this.fg.controls.connectionName.setValue(this.pipeData.connectorDetails.connectorName);
      this.fg.controls.server.setValue(obj.hostName);
      this.fg.controls.username.setValue(obj.userName);
      this.fg.controls.database.setValue(obj.databaseName);
      this.fg.controls.port.setValue(obj.port);
      this.fg.controls.password.setValue(obj.password);

      this.publishedPipeline = this.pipeData.published;
    }
  }

  updatePipeline(): void {
    this.loading = true;
    const updateParams = {
      connectorName: this.fg.value.connectionName,
      connectorId: this.connectorId,
      connectorConfig: {
        hostName: this.fg.value.server,
        userName: this.fg.value.username,
        password: this.fg.value.password,
        databaseName: this.fg.value.database,
        port: this.fg.value.port,
        serviceName: ''
      },
      connectorType: 'pgsql',
      pipelineId: this.id,
      pipelineName: this.fg.value.pipelineName,
      pipelineType: 'pgsql',
      userId: this.userId,
      projectId: this.projectId,
      schemaName: '',
      tableName: ''
    };

    this.sourceDataService
      .updatePipelineData(updateParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        if (res.code === 200) {
          this.modal.close();
          this.snakbar.open(res.message);
        } else {
          this.snakbar.open(res.message);
        }
      }, (error) => {
        this.loading = false;
      });

  }

  public testConnection(): void {
    this.loading = true;
    this.editConnectionParams = {
      pipelineName: this.fg.value.pipelineName,
      connectorName: this.fg.value.connectionName,
      connectorType: 'pgsql',
      saveDbCon: this.fg.value.accepted,
      projectId: this.projectId,
      userId: this.userId,
      schemaName: '',
      tableName: '',
      isExistingpipeline: true,
      connectorId: this.connectorId,
      pipelineId: this.id,
      connectorConfig: {
        hostName: this.fg.value.server,
        userName: this.fg.value.username,
        password: this.fg.value.password,
        databaseName: this.fg.value.database,
        port: this.fg.value.port,
        serviceName: '',
      } as any
    } as any

    this.sourceDataService
      .testConnection(this.editConnectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        this.snakbar.open(res.message);
        if (res.code === 200) {
          this.connectionStatus = true;
        } else {
          this.connectionStatus = false;
        }
      }, (error) => {
        this.snakbar.open("Connection failed!");
        this.connectionStatus = false;
        this.loading = false;
      });
  }

  public connect(): void {
    this.loading = true;

    this.sourceDataService
      .connect(this.editConnectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;

        this.modal.close();

        sessionStorage.setItem('connectionParams', JSON.stringify(this.editConnectionParams));
        this.router.navigate([`projects/${this.projectId}/ingest/select-table`]);
        this.snakbar.open(res.message);

      }, (error) => {
        this.snakbar.open(error);
        this.loading = false;
      });
  }
}
