import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject, Observable } from 'rxjs';
import { ConnectSuccessComponent } from '../connect-success/connect-success.component';
import { DbConnectorService } from '../../services/db-connector.service';
import { takeUntil } from 'rxjs/operators';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';

@Component({
  selector: 'app-connect-db',
  templateUrl: './connect-db.component.html',
  styleUrls: ['./connect-db.component.scss']
})

export class ConnectDbComponent implements OnInit, OnDestroy {
  @Input() projectId: string;
  @Input() sourceType: string;

  validConnection: boolean;
  fg: FormGroup;
  dbConnections = [];
  certificate$: Observable<Certificate>;
  connectionType: ConnectionType;
  tablesList = [];
  certificateData: Certificate;
  connectionStatus = false;
  loading: boolean;
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    public modal: NgbActiveModal,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private connectorService: DbConnectorService,
    private snackbar: SnackbarService
  ) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });
  }

  ngOnInit(): void {
    this.getExistionConnectionBySourceType();

    this.loading = true;
    this.fg = this.fb.group({
      // connectionTypeRadios: [''],
      connectionName: [''],
      server: ['', Validators.required],
      port: ['', Validators.required],
      database: ['', Validators.required],
      username: ['', Validators.required],
      password: ['', Validators.required],
      extra: [''],
      // saveDbInfo: ['', Validators.required]
    });

    this.loading = false;
  }

  getExistionConnectionBySourceType(): void {
    this.loading = true;
    this.connectorService.getConnectionBySourceType(+this.projectId, this.sourceType)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((res) => {
        this.loading = false;
        if (res?.code === 200) {
          this.connectionType = res?.result.length ? ConnectionType.Exsting : ConnectionType.New;
          this.dbConnections = res?.result;
        }
      }, (error) => {
        this.loading = false;
        this.connectionType = ConnectionType.New;
      });
  }

  handleChange(event): void {
    if (this.connectionType === ConnectionType.New) {
      this.connectionStatus = false;
      this.fg.get('connectionName').setValidators([Validators.required]);
      this.fg.get('connectionName').updateValueAndValidity();

      this.fg.controls.connectionName.setValue('');
      this.fg.controls.server.setValue('');
      this.fg.controls.port.setValue('');
      this.fg.controls.database.setValue('');
      this.fg.controls.username.setValue('');
      this.fg.controls.password.setValue('');
    }
  }

  selectedDbType(): void {
    this.loading = true;
    this.connectorService
      .getDbDetails(
        parseInt(this.certificateData.user_id, 10),
        this.fg.value.dbtype
      )
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(response => {
        this.loading = false;
        if (response.code === 200) {
          const params = JSON.parse(response.result);
          this.fg.controls.server.setValue(params.hostAddress);
          this.fg.controls.port.setValue(params.port);
          this.fg.controls.database.setValue(params.dbName);
          this.fg.controls.username.setValue(params.userName);
        }
      }, (error) => {
        this.loading = false;
      });
  }

  selectedConnectionType(): void {
    this.connectionStatus = true;
    const dataSourceId = this.fg.value.connectionName;
    const connectionValue = this.dbConnections.find(x => x?.dataSourceId == dataSourceId);

    if (connectionValue) {
      this.fg.controls.server.setValue(connectionValue?.host);
      this.fg.controls.port.setValue(connectionValue?.port);
      this.fg.controls.database.setValue(connectionValue?.schemaName);
      this.fg.controls.username.setValue(connectionValue?.userName);
      this.fg.controls.password.setValue(connectionValue?.password);
    }
  }

  testConnection(): void {
    this.loading = true;
    const connectionParams = {
      dbType: this.sourceType?.toLowerCase(),
      serviceName: this.fg.value.connectionName,
      hostName: this.fg.value.server,
      port: this.fg.value.port,
      dbName: this.fg.value.database,
      userName: this.fg.value.username,
      password: this.fg.value.password,
      projectId: this.projectId,
      userId: +this.certificateData.user_id
    };
    this.connectorService
      .testConnection(connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        this.snackbar.open(res.message);
        this.connectionStatus = true;

        // if (res.code === 200) {
        //   this.connectionStatus = true;
        //   if (this.connectionType === ConnectionType.New && this.fg.value.saveDbInfo) {
        //     this.saveConnection();
        //   }
        // }
      }, (error) => {
        this.loading = false;
      });
  }

  saveConnection() {
    const params = {
      projectId: this.projectId,
      connectionName: this.fg.value.connectionName,
      type: this.sourceType?.toLowerCase(),
      host: this.fg.value.server,
      port: this.fg.value.port,
      schemaName: this.fg.value.database,
      userName: this.fg.value.username,
      password: this.fg.value.password,
      createdBy: this.certificateData.user
    };

    this.connectorService.saveConnection(params)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((res) => {
      }, (error) => {
        this.snackbar.open(error);
      });
  }

  connect(): void {
    this.loading = true;
    const connectionParams = {
      dbName: this.fg.value.database,
      dbType: this.sourceType?.toLowerCase(),
      hostName: this.fg.value.server,
      password: this.fg.value.password,
      serviceName: this.sourceType.toLowerCase(),
      port: +this.fg.value.port,
      userName: this.fg.value.username,
      // saveDbInfo: this.fg.value.saveDbInfo,
      userId: +this.certificateData.user_id,
      projectId: +this.projectId
    };
    this.connectorService
      .connect(connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        if (res.code === 200) {
          const message = `${res.message} retrived successfully`;
          this.snackbar.open(message);
          this.tablesList = res.result;
          this.tablesList = this.tablesList.filter(x => !x.table_name?.startsWith('qs_'));
          this.showTables(connectionParams);
        } else {
          this.snackbar.open(res.message);
        }
      }, (error) => {
        this.loading = false;
      });
  }

  showTables(conParams: any): void {
    conParams.projectId = parseInt(this.projectId, 10);
    conParams.userId = parseInt(this.certificateData.user_id, 10);
    const modalRef: NgbModalRef = this.modalService.open(
      ConnectSuccessComponent,
      { size: 'lg' }
    );
    ((
      modalRef.componentInstance
    ) as ConnectSuccessComponent).tables = this.tablesList;
    modalRef.componentInstance.connectionParams = conParams;
    this.modal.dismiss('Tables List');
    modalRef.result.then(
      result => {
        const projectParam: ProjectParam = {
          userId: this.certificateData.user_id,
          folderId: '',
          projectId: this.projectId,
          file: '',
          folderName: ''
        };
        this.quantumFacade.loadProjectSource(projectParam);
        console.log(result);
      },
      reason => {
        console.log(reason);
      }
    );
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}


export enum ConnectionType {
  New = 1,
  Exsting = 2
}
