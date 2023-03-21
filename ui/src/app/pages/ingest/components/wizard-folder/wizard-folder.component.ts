import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { StpperType } from '../../enums/stepper';

import { Folder } from '../../models/folder';
import { Pii, PiiDataResult } from '../../models/pii';
import { Subject } from 'rxjs';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-wizard-folder',
  templateUrl: './wizard-folder.component.html',
  styleUrls: ['./wizard-folder.component.scss']
})
export class WizardFolderComponent implements OnInit {

  private unsubscribe: Subject<void> = new Subject();
  public fg: FormGroup;
  public stepperType = StpperType;
  public activeStep: any;
  public folder: Folder;
  public piiData: Array<Pii> = [];
  public Pii: Array<PiiDataResult> = [];
  public projectName: string;
  public projectId: number;
  public width: number = 20;
  public userId: number;

  public stepper = [
    { name: 'Go Back', step: StpperType.Back, nextStep: StpperType.CreateFolder, visible: true },
    { name: 'Create Folder', step: StpperType.CreateFolder, nextStep: StpperType.ConfigureFolder, visible: false },
    { name: 'Configure Folder', step: StpperType.ConfigureFolder, nextStep: StpperType.PiiIdentification, visible: false },
    { name: 'PII Identification', step: StpperType.PiiIdentification, nextStep: StpperType.CompletePII, visible: false },
    { name: 'Complete', step: StpperType.CompletePII, visible: false }
  ];

  constructor(
    private fb: FormBuilder,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private location: Location,
    private dataservice: DataService) {
    this.activeStep = StpperType.CreateFolder;
    this.projectName = localStorage.getItem('projectname');
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');

    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.dataservice.progress.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((progress: number) => {
      this.width = progress;
    });

    this.fg = this.fb.group({
      folderName: ['', [Validators.required, Validators.maxLength(30)]],
      folderDesc: ['', Validators.maxLength(255)],
    });
  }

  public back(): void {
    this.location.back();
  }

}
