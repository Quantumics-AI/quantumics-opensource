import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { Pii, PiiData } from '../../models/pii';
import { Folder } from '../../models/folder';
import { DataBaseStpperType } from '../../enums/stepper';
import { DbPipeLinePayload } from '../../models/dbpipeline';
import { DatabasePayload } from '../../models/database';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { DataService } from '../../services/data-service';
@Component({
  selector: 'app-db-connector',
  templateUrl: './db-connector.component.html',
  styleUrls: ['./db-connector.component.scss']
})
export class DbConnectorComponent implements OnInit, OnDestroy {
  projectId: number;
  projectName: string;
  isShown: boolean = false;

  private unsubscribe: Subject<void> = new Subject();
  public selectedDBType = DBType;
  public activeStep: any;
  public isBack: any;

  public connectionParams: DatabasePayload;
  public dbPipeLinePayload: DbPipeLinePayload;
  public piiData: Array<PiiData> = [];
  public pii: Array<Pii> = [];
  public folder: Folder;
  public width: number = 20;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  public userId: number;

  public stepper = [
    { name: 'Go Back', step: DataBaseStpperType.Back, nextStep: DataBaseStpperType.ConfigurePipeline, visible: true },
    { name: 'Configure Pipeline', step: DataBaseStpperType.ConfigurePipeline, nextStep: DataBaseStpperType.SelectTable, visible: false },
    { name: 'Select Table', step: DataBaseStpperType.SelectTable, nextStep: DataBaseStpperType.PiiIdentification, visible: false },
    { name: 'PII Identification', step: DataBaseStpperType.PiiIdentification, nextStep: DataBaseStpperType.CompletePII, visible: false },
    { name: 'Complete', step: DataBaseStpperType.CompletePII, visible: false }
  ];

  constructor(
    private activatedRoute: ActivatedRoute,
    private location: Location,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private dataservice: DataService,
  ) {
    this.folder = new Folder();
    this.activeStep = this.selectedDBType.Connection;

    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.projectName = localStorage.getItem('projectname');

    this.dataservice.progress.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((progress: number) => {
      this.width = progress;
    });
  }

  public back(): void {
    this.location.back();
  }

  ngOnDestroy() {
  }
}

export enum DBType {
  Connection = 1,
  ConfigurePipeline = 2,
  SelectTable = 3,
  PiiIdentification = 4,
  CompletePII = 5
}
